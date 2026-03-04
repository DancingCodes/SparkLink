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

	return r
}
