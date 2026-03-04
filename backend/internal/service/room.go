package service

import (
	"backend/internal/db"
	"backend/internal/model"
	"errors"

	"gorm.io/gorm"
)

// CreateRoom 创建房间（使用事务）
func CreateRoom(room *model.Room) error {
	return db.DB.Transaction(func(tx *gorm.DB) error {
		// 1. 创建房间主表记录
		if err := tx.Create(room).Error; err != nil {
			return err
		}

		// 2. 自动将房主加入房间成员表，角色设为 1 (房主)
		roomUser := model.RoomUser{
			RoomID: room.ID,
			UserID: room.OwnerID,
			Role:   1, // 房主
		}
		if err := tx.Create(&roomUser).Error; err != nil {
			return err
		}
		return nil
	})
}

// GetRoomList 获取房间大厅列表
func GetRoomList() ([]model.Room, error) {
	var rooms []model.Room
	// Preload("Owner") 可以直接带出房主的名字和头像（前提是Room结构体里有Owner User字段）
	err := db.DB.Order("created_at DESC").Find(&rooms).Error
	return rooms, err
}

// DissolveRoom 解散房间
func DissolveRoom(roomID uint, userID uint) error {
	return db.DB.Transaction(func(tx *gorm.DB) error {
		var room model.Room
		// 1. 权限校验：只有房主才能解散
		if err := tx.Where("id = ? AND owner_id = ?", roomID, userID).First(&room).Error; err != nil {
			return errors.New("无权解散该房间或房间不存在")
		}

		// 2. 软删除房间记录
		if err := tx.Delete(&room).Error; err != nil {
			return err
		}

		// 3. 清空该房间的所有成员记录（因为房间没了，成员自然就不在房间里了）
		if err := tx.Where("room_id = ?", roomID).Delete(&model.RoomUser{}).Error; err != nil {
			return err
		}

		return nil
	})
}

// JoinRoom 进入房间逻辑
func JoinRoom(roomID uint, userID uint) error {
	var roomUser model.RoomUser
	// 1. 检查是否已经在房间里（防止重复插入）
	err := db.DB.Where("room_id = ? AND user_id = ?", roomID, userID).First(&roomUser).Error
	if err == nil {
		return nil // 已经在房间里了，直接返回成功
	}

	// 2. 插入新成员记录
	newUser := model.RoomUser{
		RoomID: roomID,
		UserID: userID,
		Role:   2, // 默认普通成员
	}
	return db.DB.Create(&newUser).Error
}

// GetRoomDetail 获取房间详情（包含房主和成员列表）
func GetRoomDetail(roomID uint) (*model.Room, []model.RoomUser, error) {
	var room model.Room
	var members []model.RoomUser

	// 查询房间并预加载房主信息
	if err := db.DB.Preload("Owner").First(&room, roomID).Error; err != nil {
		return nil, nil, err
	}

	// 查询房间内的所有成员并预加载用户信息
	db.DB.Preload("User").Where("room_id = ?", roomID).Find(&members)

	return &room, members, nil
}

// LeaveRoom 离开房间
func LeaveRoom(roomID uint, userID uint) error {
	// 物理删除：用户离开房间，这条关联记录就没用了
	return db.DB.Where("room_id = ? AND user_id = ?", roomID, userID).Delete(&model.RoomUser{}).Error
}
