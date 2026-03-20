package controller

import (
	"backend/internal/model"
	"backend/internal/service"
	"backend/pkg/utils"

	"github.com/gin-gonic/gin"
	"golang.org/x/crypto/bcrypt"
)

func Register(c *gin.Context) {
	var user model.User
	if err := c.ShouldBindJSON(&user); err != nil {
		utils.Error(c, "参数错误")
		return
	}

	if err := service.RegisterUser(&user); err != nil {
		utils.Error(c, "注册失败: "+err.Error())
		return
	}
	utils.Success(c, "注册成功")
}

func Login(c *gin.Context) {
	var loginReq struct {
		Phone    string `json:"phone" binding:"required"`
		Password string `json:"password" binding:"required"`
	}

	if err := c.ShouldBindJSON(&loginReq); err != nil {
		utils.Error(c, "手机号或密码不能为空")
		return
	}

	user, err := service.LoginUser(loginReq.Phone, loginReq.Password)
	if err != nil {
		utils.Error(c, err.Error())
		return
	}

	token, err := utils.GenerateToken(user.ID)
	if err != nil {
		utils.Error(c, "生成Token失败")
		return
	}

	user.Password = ""
	utils.Success(c, gin.H{
		"token": token,
		"user":  user,
	})
}

func GetUserInfo(c *gin.Context) {
	uid, exists := c.Get("user_id")
	if !exists {
		utils.Error(c, "未找到登录信息")
		return
	}

	user, err := service.GetUserByID(uid.(uint))
	if err != nil {
		utils.Error(c, err.Error())
		return
	}

	user.Password = ""
	utils.Success(c, user)
}

func UpdateProfile(c *gin.Context) {
	// 1. 定义接收结构体
	var req struct {
		Name     string `json:"name"`
		Sex      string `json:"sex"`
		Avatar   string `json:"avatar"`
		Password string `json:"password"`
	}

	if err := c.ShouldBindJSON(&req); err != nil {
		utils.Error(c, "参数解析失败")
		return
	}

	uid := c.MustGet("user_id").(uint)

	updateData := make(map[string]interface{})
	if req.Name != "" {
		updateData["name"] = req.Name
	}
	if req.Sex != "" {
		updateData["sex"] = req.Sex
	}
	if req.Avatar != "" {
		updateData["avatar"] = req.Avatar
	}

	if req.Password != "" {
		hashedPassword, _ := bcrypt.GenerateFromPassword([]byte(req.Password), bcrypt.DefaultCost)
		updateData["password"] = string(hashedPassword)
	}

	// 5. 执行更新
	if err := service.UpdateUserInfo(uid, updateData); err != nil {
		utils.Error(c, "更新资料失败")
		return
	}

	utils.Success(c, "更新成功")
}

func CloseAccount(c *gin.Context) {
	uid, _ := c.Get("user_id")
	userID := uid.(uint)

	if err := service.SoftDeleteUser(userID); err != nil {
		utils.Error(c, "注销账户失败")
		return
	}

	utils.Success(c, "账户已注销")
}
