package model

import (
	"time"

	"gorm.io/gorm"
)

// Room 房间表
type Room struct {
	ID        uint           `gorm:"primaryKey" json:"id"`
	OwnerID   uint           `gorm:"index" json:"owner_id"`
	Title     string         `gorm:"type:varchar(255);not null" json:"title"`
	Cover     string         `gorm:"type:varchar(255)" json:"cover"`
	CreatedAt time.Time      `json:"created_at"`
	UpdatedAt time.Time      `json:"updated_at"`
	DeletedAt gorm.DeletedAt `gorm:"index" json:"-"`
}

// RoomUser 房间与用户关联表（中间表）
type RoomUser struct {
	ID     uint `gorm:"primaryKey" json:"id"`
	RoomID uint `gorm:"index" json:"room_id"`
	UserID uint `gorm:"index" json:"user_id"`
	User   User `gorm:"foreignKey:UserID" json:"user"`
	// Role: 1-房主, 2-普通成员
	Role      int       `gorm:"type:tinyint;default:2" json:"role"`
	CreatedAt time.Time `json:"entered_at"`
}
