//
//  RFIWriter.m
//  RfiFormat
//
//  Created by Mgen on 14-7-1.
//  Copyright (c) 2014年 Mgen. All rights reserved.
//

#import "RFIWriter.h"

@implementation RFIWriter

+ (instancetype)writerWithData:(NSMutableData *)data
{
    return [[RFIWriter alloc] initWithData:data];
}

- (instancetype)initWithData:(NSMutableData *)data
{
    self = [super init];
    if (!self || !data)
        return nil;
    
    _data = data;
    return self;
}

- (void)writeBytes:(NSData*)bytes
{
    if(bytes.length) [_data appendData:bytes];
}

- (void)writeBytes:(const char*)rawBytes length:(uint32_t)length
{
    if(length) [_data appendBytes:rawBytes length:length];
}

- (void)writePrefixedBytes:(NSData *)data
{
    [self writeUInt32:(uint32_t)data.length];
    [self writeBytes:data];
}

- (void)writeInt32:(int32_t)value
{
    [self writeBytes:(const char *)&value length:sizeof(int32_t)];
}

- (void)writeInt64:(int64_t)value
{
    [self writeBytes:(const char *)&value length:sizeof(int64_t)];
}

- (void)writeInt16:(int16_t)value
{
    [self writeBytes:(const char *)&value length:sizeof(int16_t)];
}

- (void)writeUInt32:(uint32_t)value
{
    [self writeBytes:(const char *)&value length:sizeof(uint32_t)];
}

- (void)writeUInt64:(uint64_t)value
{
    [self writeBytes:(const char *)&value length:sizeof(uint64_t)];
}

- (void)writeUInt16:(uint16_t)value
{
    [self writeBytes:(const char *)&value length:sizeof(uint16_t)];
}

- (void)writeByte:(char)byte
{
    [self writeBytes:(const char *)&byte length:sizeof(char)];
}

- (void)writeBool:(BOOL)value
{
    [self writeByte:value ? 1 : 0];
}

- (void)writeString:(NSString*)str
{
    [self writePrefixedBytes:[str dataUsingEncoding:NSUTF8StringEncoding]];
}

- (void)writeFloat:(float)value
{
    [self writeBytes:(const char *)&value length:sizeof(float)];
}

- (void)writeDouble:(double)value
{
    [self writeBytes:(const char *)&value length:sizeof(double)];
}

@end
