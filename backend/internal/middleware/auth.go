package middleware

import (
	"backend/pkg/utils"
	"strings"

	"github.com/gin-gonic/gin"
)

func JWTAuth() gin.HandlerFunc {
	return func(c *gin.Context) {
		whitelist := []string{
			"/user/register",
			"/user/login",
			"/common/upload",
		}

		currentPath := c.Request.URL.Path
		for _, path := range whitelist {
			if currentPath == path {
				c.Next()
				return
			}
		}

		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			utils.Unauthorized(c, "未登录，请先登录")
			c.Abort()
			return
		}

		parts := strings.SplitN(authHeader, " ", 2)
		if !(len(parts) == 2 && parts[0] == "Bearer") {
			utils.Unauthorized(c, "认证格式有误")
			c.Abort()
			return
		}

		claims, err := utils.ParseToken(parts[1])
		if err != nil {
			utils.Unauthorized(c, "无效的 Token 或已过期")
			c.Abort()
			return
		}

		c.Set("user_id", uint(claims["user_id"].(float64)))
		c.Next()
	}
}
