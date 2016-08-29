//
//  MessageDataPacketTool.h
//  mpush-client-ios(OC)
//
//  Created by Yonglin on 16/8/29.
//  Copyright © 2016年 Yonglin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "RSA.h"
#import "RFIWriter.h"
#import "Mpush.h"
#import "LFCGzipUtility.h"
typedef struct _ipbody
{
    char *deviceId;
    char *osName;
    char *osVersion;
    char *clientVersion;
    int8_t iv[16] ;
    int8_t clientKey[16];
    int minHeartbeat;
    int maxHeartbeat;
    long timestamp;
}IP_BODY;

typedef struct _iphdr
{
    uint32_t length;          //body的长度
    int8_t cmd;           //协议消息类型
    short cc;           //根据body生成的校验码
    int8_t flags;         //当前包使用的一些特性
    int sessionId;     //消息会话标示用于消息响应
    int8_t lrc;          //用于校验header
    int8_t *body;
    
}IP_PACKET;

/**
 *  握手成功的body
 */
typedef struct _handSuccessBody
{
    int8_t serverKey[16];    //服务段返回的key 用于aes加密的
    int heartbeat;     //消息会话标示用于消息响应
    char *sessionId;    //会话id
    long expireTime;   //失效时间
    
}HAND_SUCCESS_BODY;


/**
 *  握手成功的packet
 */
typedef struct _handSuccess
{
    uint32_t length;          //body的长度
    int8_t cmd;           //协议消息类型
    short cc;           //根据body生成的校验码
    int8_t flags;         //当前包使用的一些特性
    int sessionId;     //消息会话标示用于消息响应
    int8_t lrc;          //用于校验header
    int8_t *body;
    
}HAND_SUCCESS;

/**
 *  error的body
 */
typedef struct error
{
    int8_t cmd;           //协议消息类型
    int8_t code;         //错误码
    char *reason;     //错误原因
    
}ERROR_MESSAGE;

/**
 *  OK的body
 */
typedef struct OKMessage
{
    int8_t cmd;           //协议消息类型
    int8_t code;         //错误码
    char *reason;     //错误原因
    
}OK_MESSAGE;

/**
 *  握手成功的body
 */
typedef struct _httpResponesBody
{
    int statusCode;     //状态码
    char *reasonPhrase;    //会话id
    char *headers;    //会话id
    int8_t *body;   //响应体
    
}HTTP_RESPONES_BODY;

typedef NS_ENUM(NSInteger, MpushMessageBodyCMD) {
    MpushMessageBodyCMDHandShakeSuccess = 2,	// 握手成功
    MpushMessageBodyCMDLogin = 3,    // 登录
    MpushMessageBodyCMDLogout = 4,   //退出
    MpushMessageBodyCMDBind = 5,    // 绑定
    MpushMessageBodyCMDUNbind = 6,    // 解除绑定
    MpushMessageBodyCMDUNFastConnect = 7,    //快速重连
    MpushMessageBodyCMDUNStop = 8,    //暂停
    MpushMessageBodyCMDUNResume = 9,    // 重新开始
    MpushMessageBodyCMDUNError = 10,    // 错误
    MpushMessageBodyCMDOk= 11,    //OK
    MpushMessageBodyCMDHttp = 12,    // Http
    MpushMessageBodyCMDPush = 15,    // 推送
    MpushMessageBodyCMDChat = 19,    // 聊天
};



//static NSString *const pubkey = @"-------BEGIN PUBLIC KEY-----\nMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7\niViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2f\nBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/j\nMv2Ggb2XAeZhlLD28fHwIDAQAB\n-----END PUBLIC KEY-----";

static NSString *const pubkey = @"-------BEGIN PUBLIC KEY-----\nMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCdIyULaS9da19R+tKuCS/LRU9a\nXfAzV8ek4FvhxgG5Az9B2eAoKHsjddwzACt9b1CtkLTnzub/SEToZEhnrq2HKhX\n2zKtHVmhwAIpYobIlYm5Lq0fWOWGR1+FqFMXoHa99DV8wm/+FS+34DS3uvoMgN5\nKYOntT9KTt+WFNH2tLhwIDAQAB\n-----END PUBLIC KEY-----";

@interface MessageDataPacketTool : NSObject

/**
 *  握手数据包
 *
 *  @return 握手数据data
 */
+ (NSData *)withPacketAndIpBody;

/**
 *  响应包信息
 *
 *  @param data read的data
 *
 *  @return ip协议包（结构体类型）
 */

+ (IP_PACKET)handShakeSuccessResponesWithData:(NSData *)data;

/**
 *  握手成功解析body
 *
 *  @param bodyData <#bodyData description#>
 *
 *  @return <#return value description#>
 */
+ (HAND_SUCCESS_BODY) HandSuccessBodyDataWithData:(NSData *)body_data andPacket:(IP_PACKET)packet;
/**
 *  心跳包
 *
 *  @return 心跳data
 */

+ (NSData *)heartbeatPacketData;
/**
 *  会话加密所需key （混淆）
 *
 *  @param clientKey 随机生成的16为byte数组
 *  @param serverKey 握手成功返回的serverKey
 *
 *  @return 混淆后的sessionKey
 */
//+ (NSData *)mixKeyWithClientKey:(int8_t [])clientKey andServerKey:(int8_t [])serverKey;

/**
 *  绑定用户id
 *
 *  @param userId 用户id
 */
+ (NSData *)bindDataWithUserId:(NSString *)userId;


/**
 *  聊天消息数据包
 *
 *  @param messageBody             聊天消息的内容
 *
 *  @return     完整的聊天数据包
 */
+ (NSData *)chatDataWithBody:(NSData *)messageBody andUrlStr:(NSString *)urlStr;

/**
 *  请求成功
 *
 *  @param bodyData 发送成功的bodyData
 *
 *  @return 发送消息成功的body（结构体）
 */
+ (HTTP_RESPONES_BODY)chatDataSuccessWithData:(NSData *)bodyData;


/**
 *  错误信息
 *
 *  @param body 错误信息body
 *
 *  @return 错误信息（结构体）
 */
+ (ERROR_MESSAGE) errorWithBody:(NSData *)body;

/**
 *  ok信息
 *
 *  @param body ok信息body
 *
 *  @return ok信息（结构体）
 */
+ (OK_MESSAGE) okWithBody:(NSData *)body;



/**
 *  aes加密方法
 *
 *  @param enData 需要加密的数据
 *  @param iv     加密指数
 *  @param key    加密key
 *
 *  @return 加密后的data
 */
+ (NSData *) aesEncriptData:(NSData *)enData WithIv:(int8_t [])iv andKey:(int8_t [])key;

/**
 *  aes解密方法
 *
 *  @param enData 需要解密的数据
 *  @param iv     加密指数
 *  @param key    加密key
 *
 *  @return 解密后的data
 */
+ (NSData *) aesDecriptWithEncryptData:(NSData *)encryptData withIv:(int8_t [])iv andKey:(int8_t[])key;
/**
 *  处理收到的push消息
 *
 *  @param packet    协议包
 *  @param body_data 协议包的body data
 *
 *  @return 消息内容
 */
+ (id)processRecievePushMessageWithPacket:(IP_PACKET)packet andData:(NSData *)body_data;

/**
 *  根据flag对body做相应处理
 *
 *  @param packet    协议包
 *  @param body_data 协议包的body data
 *
 *  @return 处理后的 body data
 */
+ (NSData *) processFlagWithPacket:(IP_PACKET)packet andBodyData:(NSData *)body_data;




@end





