package model

import (
	"time"

	"gorm.io/gorm"
)

type User struct {
	ID        uint           `gorm:"primaryKey" json:"id"`
	Phone     string         `gorm:"type:varchar(255);uniqueIndex" json:"phone"`
	Password  string         `gorm:"type:varchar(255)" json:"password"`
	Name      string         `gorm:"type:varchar(255)" json:"name"`
	Sex       string         `gorm:"type:varchar(255)" json:"sex"`
	Avatar    string         `gorm:"type:varchar(255)" json:"avatar"`
	CreatedAt time.Time      `json:"created_at"`
	UpdatedAt time.Time      `json:"updated_at"`
	DeletedAt gorm.DeletedAt `gorm:"index" json:"-"`
}
