package controller

import (
	"backend/internal/model"
	"backend/internal/service"
	"backend/pkg/utils"
	"fmt"
	"strconv"

	"github.com/gin-gonic/gin"
)

func CreateRoom(c *gin.Context) {
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

func GetRoomList(c *gin.Context) {
	rooms, err := service.GetRoomList()
	if err != nil {
		utils.Error(c, "获取列表失败")
		return
	}
	utils.Success(c, rooms)
}

func DissolveRoom(c *gin.Context) {
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
	utils.Success(c, nil)
}

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

	token, err := utils.GenerateVoiceToken(req.RoomID, uid)
	if err != nil {
		utils.Error(c, "生成语音令牌失败，请重试")
		return
	}

	utils.Success(c, gin.H{
		"agora_token":  token,
		"agora_uid":    uid,
		"channel_name": fmt.Sprintf("room_%d", req.RoomID),
	})
}

func GetRoomInfo(c *gin.Context) {
	roomIDStr := c.Param("id")

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

func QuitRoom(c *gin.Context) {
	var req struct {
		RoomID uint `json:"room_id" binding:"required"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.Error(c, "参数解析失败")
		return
	}

	uid := c.MustGet("user_id").(uint)

	if err := service.LeaveRoom(req.RoomID, uid); err != nil {
		utils.Error(c, "退出房间失败")
		return
	}

	utils.Success(c, nil)
}
