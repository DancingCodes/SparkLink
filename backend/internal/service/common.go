package service

import (
	"backend/pkg/utils"
	"mime/multipart"
)

type FileHubResponse struct {
	Code int    `json:"code"`
	Msg  string `json:"msg"`
	Data struct {
		ID        int    `json:"id"`
		FileName  string `json:"file_name"`
		FileUUID  string `json:"file_uuid"`
		FileSize  int    `json:"file_size"`
		FileType  string `json:"file_type"`
		CreatedAt string `json:"created_at"`
		FileUrl   string `json:"file_url"`
	} `json:"data"`
}

func ForwardToFileHub(fileHeader *multipart.FileHeader) (*FileHubResponse, error) {
	var result FileHubResponse
	targetURL := "https://filehub.moonc.love/api/upload"

	err := utils.PostMultipart(targetURL, fileHeader, &result)
	if err != nil {
		return nil, err
	}

	return &result, nil
}
