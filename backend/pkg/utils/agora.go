package utils

import (
	"fmt"
	"os"

	rtctokenbuilder "github.com/AgoraIO/Tools/DynamicKey/AgoraDynamicKey/go/src/rtctokenbuilder2"
)

func GenerateVoiceToken(roomID uint, userID uint) (string, error) {
	appId := os.Getenv("AGORA_APP_ID")
	appCertificate := os.Getenv("AGORA_APP_CERTIFICATE")
	if appId == "" || appCertificate == "" {
		return "", fmt.Errorf("声网配置未设置: AGORA_APP_ID 或 AGORA_APP_CERTIFICATE 为空")
	}

	channelName := fmt.Sprintf("room_%d", roomID)
	uid := uint32(userID)

	// Token 的有效时间，单位秒
	tokenExpirationInSeconds := uint32(86400)
	// 所有的权限的有效时间，单位秒，声网建议你将该参数和 Token 的有效时间设为一致
	privilegeExpirationInSeconds := uint32(86400)

	// 生成 Token
	result, err := rtctokenbuilder.BuildTokenWithUid(
		appId,
		appCertificate,
		channelName,
		uid,
		rtctokenbuilder.RolePublisher,
		tokenExpirationInSeconds,
		privilegeExpirationInSeconds,
	)
	if err != nil {
		fmt.Println(err)
	}

	return result, nil
}
