package model

import (
	"time"

	"gorm.io/gorm"
)

// Room 房间表
type Room struct {
	ID        uint           `gorm:"primaryKey" json:"id"`
	OwnerID   uint           `gorm:"index" json:"owner_id"`
	Owner     User           `gorm:"foreignKey:OwnerID" json:"owner"`
	Title     string         `gorm:"type:varchar(255);not null" json:"title"`
	Cover     string         `gorm:"type:varchar(255)" json:"cover"`
	CreatedAt time.Time      `json:"created_at"`
	UpdatedAt time.Time      `json:"updated_at"`
	DeletedAt gorm.DeletedAt `gorm:"index" json:"-"`
}
