package router

import (
	"backend/internal/controller"

	"github.com/gin-gonic/gin"
)

func SetupRouter() *gin.Engine {
	r := gin.Default()
	//r.Use(middleware.JWTAuth())

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
		room.POST("/create", controller.CreateRoom)     // 创建
		room.GET("/list", controller.GetRoomList)       // 列表（大厅）
		room.POST("/dissolve", controller.DissolveRoom) // 解散
		room.POST("/enter", controller.EnterRoom)       // 进入
		room.GET("/info/:id", controller.GetRoomInfo)   // 详情
	}

	ws := r.Group("/ws")
	{
		ws.GET("/room", controller.RoomWS)
	}
	return r
}
