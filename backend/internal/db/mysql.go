package db

import (
	"backend/internal/model"
	"log"
	"os"

	"gorm.io/driver/mysql"
	"gorm.io/gorm"
)

var DB *gorm.DB

func InitDB() {
	dsn := os.Getenv("DB_DSN")
	if dsn == "" {
		log.Fatal("环境变量 DB_DSN 未设置")
	}

	var err error
	DB, err = gorm.Open(mysql.Open(dsn), &gorm.Config{})
	if err != nil {
		log.Fatalf("数据库连接失败: %v", err)
	}

	// 自动迁移
	if err := DB.AutoMigrate(&model.User{}); err != nil {
		log.Fatalf("数据库创建失败: %v", err)
	}
	log.Println("数据库连接成功并已同步表结构")
}
