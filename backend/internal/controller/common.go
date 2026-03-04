package controller

import (
	"backend/internal/service"
	"backend/pkg/utils"

	"github.com/gin-gonic/gin"
)

func Upload(c *gin.Context) {
	file, err := c.FormFile("file")
	if err != nil {
		utils.Error(c, "上传文件不能为空")
		return
	}

	// 2. 调用通用转发 Service
	res, err := service.ForwardToFileHub(file)
	if err != nil {
		utils.Error(c, "文件转发失败: "+err.Error())
		return
	}

	// 3. 返回 FileHub 的数据给前端
	if res.Code != 200 {
		utils.Error(c, res.Msg)
		return
	}

	utils.Success(c, res.Data)
}
