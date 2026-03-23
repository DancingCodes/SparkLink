package controller

import (
	"backend/internal/service"
	"net/http"
	"strconv"
	"sync"

	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
)

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool {
		return true
	},
}

type RoomHub struct {
	rooms map[string]map[uint]*websocket.Conn
	mu    sync.Mutex
}

var Hub = &RoomHub{
	rooms: make(map[string]map[uint]*websocket.Conn),
}

func (h *RoomHub) Broadcast(roomID string, message interface{}) {
	h.mu.Lock()
	defer h.mu.Unlock()
	if clients, ok := h.rooms[roomID]; ok {
		for _, conn := range clients {
			_ = conn.WriteJSON(message)
		}
	}
}

func RoomWS(c *gin.Context) {
	roomIDStr := c.Query("room_id")
	uidStr := c.Query("uid")

	roomIDInt, _ := strconv.Atoi(roomIDStr)
	roomID := uint(roomIDInt)
	uidInt, _ := strconv.Atoi(uidStr)
	uid := uint(uidInt)

	// 将普通的 HTTP 请求升级为 WebSocket 长连接
	conn, err := upgrader.Upgrade(c.Writer, c.Request, nil)
	if err != nil {
		return
	}

	defer func() {
		// A. 断开连接
		err := conn.Close()
		if err != nil {
			return
		}

		// B. 从内存 Hub 移除 (停止对其广播)
		Hub.mu.Lock()
		if Hub.rooms[roomIDStr] != nil {
			delete(Hub.rooms[roomIDStr], uid)
		}
		Hub.mu.Unlock()

		// C. 数据库同步 (标记用户离开)
		_ = service.LeaveRoom(roomID, uid)

		// D. 广播：告诉别人我走了
		Hub.Broadcast(roomIDStr, gin.H{
			"type": "leave",
			"uid":  uid,
		})
	}()

	// 3. 进入业务逻辑
	// A. 数据库登记
	if err := service.JoinRoom(roomID, uid); err != nil {
		_ = conn.WriteJSON(gin.H{"type": "error", "msg": "进房失败"})
		return
	}

	// B. 加入内存池
	Hub.mu.Lock()
	if Hub.rooms[roomIDStr] == nil {
		Hub.rooms[roomIDStr] = make(map[uint]*websocket.Conn)
	}
	Hub.rooms[roomIDStr][uid] = conn
	Hub.mu.Unlock()

	// C. 广播：告诉别人我来了
	Hub.Broadcast(roomIDStr, gin.H{
		"type": "join",
		"uid":  uid,
	})

	// 4. 维持连接 (阻塞)
	for {
		// 这里可以加一个读取超时，比如 60 秒没消息就断开
		if _, _, err := conn.ReadMessage(); err != nil {
			break
		}
	}
}
