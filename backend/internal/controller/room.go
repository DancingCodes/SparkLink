package controller

import (
	"backend/internal/model"
	"backend/internal/service"
	"backend/pkg/utils"
	"strconv"

	"github.com/gin-gonic/gin"
)

// HandleCreateRoom POST /room/create
func HandleCreateRoom(c *gin.Context) {
	var req struct {
		Title string `json:"title" binding:"required"`
		Cover string `json:"cover"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.Error(c, "标题是必填项")
		return
	}

	uid := c.MustGet("user_id").(uint)

	room := model.Room{
		Title:   req.Title,
		Cover:   req.Cover,
		OwnerID: uid,
	}

	if err := service.CreateRoom(&room); err != nil {
		utils.Error(c, "创建失败")
		return
	}
	utils.Success(c, room)
}

// HandleGetRoomList GET /room/list
func HandleGetRoomList(c *gin.Context) {
	rooms, err := service.GetRoomList()
	if err != nil {
		utils.Error(c, "获取列表失败")
		return
	}
	utils.Success(c, rooms)
}

// HandleDissolveRoom POST /room/dissolve
func HandleDissolveRoom(c *gin.Context) {
	var req struct {
		RoomID uint `json:"room_id" binding:"required"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.Error(c, "参数错误")
		return
	}

	uid := c.MustGet("user_id").(uint)

	if err := service.DissolveRoom(req.RoomID, uid); err != nil {
		utils.Error(c, err.Error())
		return
	}
	utils.Success(c, "房间已成功解散")
}

// EnterRoom 接口：POST /room/enter
func EnterRoom(c *gin.Context) {
	var req struct {
		RoomID uint `json:"room_id" binding:"required"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.Error(c, "参数错误")
		return
	}

	uid := c.MustGet("user_id").(uint)

	if err := service.JoinRoom(req.RoomID, uid); err != nil {
		utils.Error(c, "进入房间失败")
		return
	}
	utils.Success(c, "成功进入房间")
}

// GetRoomInfo 接口：GET /room/info/:id
func GetRoomInfo(c *gin.Context) {
	roomIDStr := c.Param("id") // 获取的是字符串 "123"

	// 修复：将字符串转换为 uint
	id, err := strconv.ParseUint(roomIDStr, 10, 64)
	if err != nil {
		utils.Error(c, "无效的房间ID")
		return
	}
	roomID := uint(id)

	room, members, err := service.GetRoomDetail(roomID)
	if err != nil {
		utils.Error(c, "房间不存在")
		return
	}

	utils.Success(c, gin.H{
		"room":    room,
		"members": members,
	})
}

// QuitRoom 接口：POST /room/leave
func QuitRoom(c *gin.Context) {
	var req struct {
		RoomID uint `json:"room_id" binding:"required"`
	}
	// 修复：处理 ShouldBindJSON 的返回值错误
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.Error(c, "参数解析失败")
		return
	}

	uid := c.MustGet("user_id").(uint)

	// 修复：处理 LeaveRoom 的返回值错误
	if err := service.LeaveRoom(req.RoomID, uid); err != nil {
		utils.Error(c, "退出房间失败")
		return
	}

	utils.Success(c, "已离开房间")
}
