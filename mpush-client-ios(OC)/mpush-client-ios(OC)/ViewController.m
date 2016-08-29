//
//  ViewController.m
//  mpush-client-ios(OC)
//
//  Created by Yonglin on 16/8/29.
//  Copyright © 2016年 Yonglin. All rights reserved.
//

#import "ViewController.h"
#import "GCDAsyncSocket.h"
#import "AFNetworking.h"
#import "RSA.h"
#import "RFIWriter.h"
#import "MessageDataPacketTool.h"
#import <CommonCrypto/CommonCryptor.h>
#import "LFCGzipUtility.h"





@interface ViewController ()<UITextFieldDelegate,UITableViewDelegate,UITableViewDataSource,GCDAsyncSocketDelegate>

@property (weak, nonatomic) IBOutlet UITextField *messageTextField;

@property (weak, nonatomic) IBOutlet UITableView *messageTableView;

/** 盛放消息内容的数组  */
@property(nonatomic,strong)NSMutableArray *messages;


@property(nonatomic,strong)GCDAsyncSocket *socket;
/**  发送心跳的计时器 */
@property(nonatomic,strong)NSTimer *timer;
/**  一条消息接收到的次数（半包处理） */
@property(nonatomic,assign)int recieveNum;
/**  接收到消息的body Data */
@property(nonatomic,strong)NSMutableData *messageBodyData;

@end

@implementation ViewController

- (NSMutableArray *)messages{
    if (_messages == nil) {
        _messages = [[NSMutableArray alloc] init];
    }
    return _messages ;
}


- (IBAction)connectBtnClick:(id)sender {
    NSLog(@"connectBtnClick");
    // 1.建立连接
    //    NSString *host = @"180.150.191.8";
    //    int port = 3000;
    
    NSString *host = @"106.75.7.156";
    int port = 3000;
    
    // 创建一个Socket对象
    _socket = [[GCDAsyncSocket alloc] initWithDelegate:self delegateQueue:dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0)];
    
    // 连接
    NSError *error = nil;
    [_socket connectToHost:host onPort:port error:&error];
    
}

- (IBAction)bindBtnClick:(id)sender {
    NSLog(@"bindBtnClick");
    
}
- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    [self.messageTextField endEditing:YES];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    
    self.messageTableView.dataSource = self;
    self.messageTableView.delegate = self;
    self.messageTextField.delegate = self;
}

#pragma mark -UITextFieldDelegate
// 发送消息
- (BOOL)textFieldShouldReturn:(UITextField *)textField{
    
    NSLog(@"textFieldShouldReturn--%@",textField.text);
    [self.messages addObject:textField.text];
    
    NSMutableData *dataaa = [NSMutableData data];
    NSString *str = textField.text;
    NSData *strData = [str dataUsingEncoding:NSUTF8StringEncoding];
    
    short strDataLength = (short)strData.length;
    HTONS(strDataLength);
    NSData *strDataLengthData = [NSData dataWithBytes:&strDataLength length:sizeof(strDataLength)];
    [dataaa appendData:strDataLengthData];
    [dataaa appendData:strData];
    
    NSString *urlStr = [NSString stringWithFormat:@"%@cafe/sendMessage.do?",CAFE_HOST_ADDRESS];
    
    [self.socket writeData:[MessageDataPacketTool chatDataWithBody:dataaa andUrlStr:urlStr] withTimeout:-1 tag:222];
    
    
    textField.text = nil;
    
    [self.messageTableView reloadData];
    
    return YES;
}

#pragma mark - UITableViewDelegate and UITableViewDataSource
- (NSInteger)tableView:(UITableView *)tableView sectionForSectionIndexTitle:(NSString *)title atIndex:(NSInteger)index{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section{
    return self.messages.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{
    static NSString *cellId = @"cellId";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellId];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:cellId];
    }
    cell.textLabel.text = self.messages[indexPath.row];
    return cell;
}

#pragma mark -GCDAsyncSocketDelegate

// 连接主机成功
-(void)socket:(GCDAsyncSocket *)sock didConnectToHost:(NSString *)host port:(uint16_t)port{
    NSLog(@"连接主机成功");
    self.socket = sock;
    // 发送协议报文
    [sock writeData:[MessageDataPacketTool withPacketAndIpBody] withTimeout:-1 tag:222];
    
}

// 与主机断开连接
-(void)socketDidDisconnect:(GCDAsyncSocket *)sock withError:(NSError *)err{
    if(err){
        NSLog(@"断开连接 %@",err);
    }
}

// 数据成功发送到服务器
-(void)socket:(GCDAsyncSocket *)sock didWriteDataWithTag:(long)tag{
    NSLog(@"数据成功发送到服务器");
    //数据发送成功后，自己调用一下读取数据的方法，接着_socket才会调用下面的代理方法
    [_socket readDataWithTimeout:-1 tag:tag];
}

// 读取到数据时调用
-(void)socket:(GCDAsyncSocket *)sock didReadData:(NSData *)data withTag:(long)tag{
    
    NSLog(@"data--%@",data);
    //心跳
    if (data.length == 1) {
        return ;
    }
    
    // 半包处理
    int length = 0;
    if (_recieveNum < 1) {
        
        NSData *lengthData = [data subdataWithRange:NSMakeRange(0, 4)];
        [lengthData getBytes: &length length: sizeof(length)];
        NTOHL(length);
    }
    
    if (length > data.length - 13) {
        _recieveNum ++ ;
        [self.messageBodyData appendData:data];
        length = 0;
        return;
    }
    
    [self.messageBodyData appendData:data];
    
    length = 0;
    _recieveNum = 0;
    
//   IP_PACKET packet = [MessageDataPacketTool handShakeSuccessResponesWithData:self.messageBodyData];
    
    IP_PACKET packet ;
    if (self.messageBodyData == nil) {
        //读取到的数据
        packet = [MessageDataPacketTool handShakeSuccessResponesWithData:data];
    } else {
        packet = [MessageDataPacketTool handShakeSuccessResponesWithData:self.messageBodyData];
    }
    self.messageBodyData = nil;
    
    //解密以前的body
    NSData *body_data = [NSData dataWithBytes:packet.body length:packet.length];
    NSLog(@"bodyData--%@--%d",body_data,packet.length);
    switch (packet.cmd) {
            
        case MpushMessageBodyCMDHandShakeSuccess:
            
            [self processHandShakeDataWithPacket:packet andData:body_data];
            break;
            
        case MpushMessageBodyCMDLogin: //登录
            
            break;
            
        case MpushMessageBodyCMDLogout: //退出
            
            break;
        case MpushMessageBodyCMDBind: //绑定
            
            break;
        case MpushMessageBodyCMDUNbind: //解绑
            
            break;
        case MpushMessageBodyCMDUNFastConnect: //快速重连
            
            break;
        case MpushMessageBodyCMDUNStop: //暂停
            
            break;
        case MpushMessageBodyCMDUNResume: //重新开始
            
            break;
        case MpushMessageBodyCMDUNError: //错误
            [MessageDataPacketTool errorWithBody:body_data];
            break;
        case MpushMessageBodyCMDOk: //ok
            //            [MessageDataPacketTool okWithBody:body_data];
            NSLog(@"绑定成功--ok===============");
            break;
            
        case MpushMessageBodyCMDHttp: // http代理
        {
            NSLog(@"ok======聊天=========");
            NSData *bodyData = [MessageDataPacketTool processFlagWithPacket:packet andBodyData:body_data];
            HTTP_RESPONES_BODY responesBody = [MessageDataPacketTool chatDataSuccessWithData:bodyData];
        }
            break;
        case MpushMessageBodyCMDPush:  //收到的push消息
            [self processRecievePushMessageWithPacket:packet andData:body_data];
            
            break;
            
        case MpushMessageBodyCMDChat: //聊天
            break;
            
        default:
            break;
    }
    
}
/**
 *  心跳
 */
- (void)heartbeatSend{
    
    [_socket writeData:[MessageDataPacketTool heartbeatPacketData] withTimeout:-1 tag:123];
}

/**
 *  处理收到的 push消息
 *
 *  @param packet    协议包
 *  @param body_data 协议包body data
 */
- (void)processRecievePushMessageWithPacket:(IP_PACKET)packet andData:(NSData *)body_data{
    id content = [MessageDataPacketTool processRecievePushMessageWithPacket:packet andData:body_data];
    NSLog(@"content--%@",content);
}

/**
 *  处理心跳响应的数据
 *
 *  @param bodyData 握手ok的bodyData
 */
- (void) processHeartDataWithData:(NSData *)bodyData{
    NSLog(@"接收到心跳");
}

/**
 *  处理握手ok响应的数据
 *
 *  @param bodyData 握手ok的bodyData
 */
- (void) processHandShakeDataWithPacket:(IP_PACKET)packet andData:(NSData *)body_data{
    
    HAND_SUCCESS_BODY handSuccessBody = [MessageDataPacketTool HandSuccessBodyDataWithData:body_data andPacket:packet];
    
    //添加计时器
    _timer = [NSTimer timerWithTimeInterval:handSuccessBody.heartbeat/1000.0 target:self selector:@selector(heartbeatSend) userInfo:nil repeats:YES];
    [[NSRunLoop mainRunLoop] addTimer:_timer forMode:NSDefaultRunLoopMode];
    
    //绑定用户
    [self.socket writeData:[MessageDataPacketTool bindDataWithUserId:[NSString stringWithFormat:@"%@",@111]] withTimeout:-1 tag:222];
}



@end









