package router

import (
	"backend/internal/controller"
	"backend/internal/middleware"

	"github.com/gin-gonic/gin"
)

func SetupRouter() *gin.Engine {
	r := gin.Default()
	r.Use(middleware.JWTAuth())

	common := r.Group("/common")
	{
		common.POST("/upload", controller.Upload)
	}

	auth := r.Group("/user")
	{
		auth.POST("/register", controller.Register)
		auth.POST("/login", controller.Login)
		auth.GET("/info", controller.GetUserInfo)
		auth.POST("/update", controller.UpdateProfile)
		auth.POST("/close", controller.CloseAccount)
	}

	room := r.Group("/room")
	{
		room.POST("/create", controller.HandleCreateRoom)     // 创建
		room.GET("/list", controller.HandleGetRoomList)       // 列表（大厅）
		room.POST("/dissolve", controller.HandleDissolveRoom) // 解散
		room.POST("/enter", controller.EnterRoom)             // 进入
		room.GET("/info/:id", controller.GetRoomInfo)         // 详情
		room.POST("/leave", controller.QuitRoom)              // 离开
	}

	return r
}
