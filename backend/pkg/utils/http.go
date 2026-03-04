package utils

import (
	"bytes"
	"encoding/json"
	"io"
	"mime/multipart"
	"net/http"
	"time"
)

// PostMultipart 封装通用的文件上传请求
func PostMultipart(url string, fileHeader *multipart.FileHeader, result interface{}) (err error) {
	src, err := fileHeader.Open()
	if err != nil {
		return err
	}
	// 使用匿名函数处理 defer 中的错误
	defer func() {
		if closeErr := src.Close(); closeErr != nil && err == nil {
			err = closeErr
		}
	}()

	body := &bytes.Buffer{}
	writer := multipart.NewWriter(body)

	part, err := writer.CreateFormFile("file", fileHeader.Filename)
	if err != nil {
		return err
	}

	if _, err = io.Copy(part, src); err != nil {
		return err
	}

	if err = writer.Close(); err != nil {
		return err
	}

	req, err := http.NewRequest("POST", url, body)
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", writer.FormDataContentType())

	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return err
	}
	
	defer func() {
		if closeErr := resp.Body.Close(); closeErr != nil && err == nil {
			err = closeErr
		}
	}()

	return json.NewDecoder(resp.Body).Decode(result)
}
