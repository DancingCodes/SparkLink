package service

import (
	"backend/internal/db"
	"backend/internal/model"
	"errors"

	"golang.org/x/crypto/bcrypt"
)

func RegisterUser(user *model.User) error {
	// 密码加密
	hashedPassword, _ := bcrypt.GenerateFromPassword([]byte(user.Password), bcrypt.DefaultCost)
	user.Password = string(hashedPassword)

	// 写入数据库
	return db.DB.Create(user).Error
}

func LoginUser(phone, password string) (*model.User, error) {
	var user model.User
	if err := db.DB.Where("phone = ?", phone).First(&user).Error; err != nil {
		return nil, errors.New("用户不存在")
	}

	// 验证密码
	err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(password))
	if err != nil {
		return nil, errors.New("密码错误")
	}
	return &user, nil
}

func GetUserByID(id uint) (*model.User, error) {
	var user model.User
	// 使用 First 查找，如果找不到会返回错误
	if err := db.DB.First(&user, id).Error; err != nil {
		return nil, errors.New("用户不存在")
	}
	return &user, nil
}

func UpdateUserInfo(uid uint, data interface{}) error {
	return db.DB.Model(&model.User{}).Where("id = ?", uid).Updates(data).Error
}

func SoftDeleteUser(uid uint) error {
	// 默认即为软删除：将 deleted_at 设置为当前时间
	return db.DB.Delete(&model.User{}, uid).Error
}
