package service

import (
	"backend/internal/db"
	"backend/internal/model"
	"errors"

	"gorm.io/gorm"
)

// CreateRoom 创建房间（使用事务）
func CreateRoom(room *model.Room) error {
	// 开启数据库事务
	return db.DB.Transaction(func(tx *gorm.DB) error {
		// 1. 先创建房间，拿到生成的房间 ID
		if err := tx.Create(room).Error; err != nil {
			return err
		}

		// 2. 将创建者（房主）插入到 room_users 关联表
		roomUser := model.RoomUser{
			RoomID: room.ID,      // 这里拿到的就是刚生成的房间主键
			UserID: room.OwnerID, // 创建者 ID
			Role:   1,            // 设置为房主身份
		}

		if err := tx.Create(&roomUser).Error; err != nil {
			return err
		}

		return nil // 返回 nil 提交事务
	})
}

// GetRoomList 获取房间大厅列表
func GetRoomList() ([]model.Room, error) {
	var rooms []model.Room
	err := db.DB.Preload("Owner").Order("created_at DESC").Find(&rooms).Error
	return rooms, err
}

// DissolveRoom 解散房间
func DissolveRoom(roomID uint, userID uint) error {
	return db.DB.Transaction(func(tx *gorm.DB) error {
		// 1. 确认身份
		var room model.Room
		if err := tx.Where("id = ? AND owner_id = ?", roomID, userID).First(&room).Error; err != nil {
			return errors.New("无权解散该房间")
		}

		// 2. 软删除房间内的所有成员关系
		// GORM 发现模型里有 DeletedAt 字段，会自动执行 UPDATE 语句而非 DELETE
		if err := tx.Where("room_id = ?", roomID).Delete(&model.RoomUser{}).Error; err != nil {
			return err
		}

		// 3. 软删除房间本身
		if err := tx.Delete(&room).Error; err != nil {
			return err
		}

		return nil
	})
}

// JoinRoom 进入房间逻辑
func JoinRoom(roomID uint, userID uint) error {
	var roomUser model.RoomUser

	// 检查是否已经在房间里（GORM 默认只查 DeletedAt IS NULL 的）
	err := db.DB.Where("room_id = ? AND user_id = ?", roomID, userID).First(&roomUser).Error

	if err == nil {
		// 已经在房间里，直接返回
		return nil
	}

	// 如果之前离开过（有软删除记录），重新进来时直接创建新记录即可
	// 或者你可以用 Unscoped().Where(...) 找到旧记录并把 DeletedAt 置空（恢复）
	newUser := model.RoomUser{
		RoomID: roomID,
		UserID: userID,
		Role:   2,
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
