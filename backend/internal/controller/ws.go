package controller

import (
	"backend/internal/service"
	"net/http"
	"strconv"
	"sync"

	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
)

// 允许跨域
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

// Broadcast 把消息同时发给房间里的所有人
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

	room, _, err := service.GetRoomDetail(roomID)
	if err != nil {
		// 如果房间不存在，直接返回，不升级协议
		return
	}
	isOwner := room.OwnerID == uid
	conn, err := upgrader.Upgrade(c.Writer, c.Request, nil)
	if err != nil {
		return
	}

	// 退出清理逻辑
	defer func() {
		_ = conn.Close()

		Hub.mu.Lock()
		if Hub.rooms[roomIDStr] != nil {
			delete(Hub.rooms[roomIDStr], uid)
		}
		Hub.mu.Unlock()

		if isOwner {
			_ = service.DissolveRoom(roomID, uid)
			Hub.Broadcast(roomIDStr, gin.H{
				"type": "dissolve",
				"uid":  uid,
			})

			Hub.CloseRoom(roomIDStr)
		} else {
			_ = service.LeaveRoom(roomID, uid)
			Hub.Broadcast(roomIDStr, gin.H{
				"type": "leave",
				"uid":  uid,
			})
		}
	}()

	if err := service.JoinRoom(roomID, uid); err != nil {
		_ = conn.WriteJSON(gin.H{"type": "error", "msg": "进房失败"})
		return
	}

	Hub.mu.Lock()
	if Hub.rooms[roomIDStr] == nil {
		Hub.rooms[roomIDStr] = make(map[uint]*websocket.Conn)
	}
	Hub.rooms[roomIDStr][uid] = conn
	Hub.mu.Unlock()

	Hub.Broadcast(roomIDStr, gin.H{
		"type": "join",
		"uid":  uid,
	})

	for {
		if _, _, err := conn.ReadMessage(); err != nil {
			break
		}
	}
}

func (h *RoomHub) CloseRoom(roomID string) {
	h.mu.Lock()
	defer h.mu.Unlock()

	if clients, ok := h.rooms[roomID]; ok {
		for uid, conn := range clients {
			_ = conn.Close()
			delete(clients, uid)
		}
		delete(h.rooms, roomID)
	}
}
