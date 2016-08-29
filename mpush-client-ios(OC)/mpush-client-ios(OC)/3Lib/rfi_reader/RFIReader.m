//
//  RFIReader.m
//  RfiFormat
//
//  Created by Mgen on 14-7-1.
//  Copyright (c) 2014年 Mgen. All rights reserved.
//

#import "RFIReader.h"

@implementation RFIReader

+ (instancetype)readerWithData:(NSData *)data
{
    return [[RFIReader alloc] initWithData:data];
}

- (instancetype)initWithData:(NSData*)data
{
    self = [super init];
    if (!self || !data)
        return nil;
    
    _data = data;
    _pointer = (char*)data.bytes;
    return self;
}

- (NSData*)readBytes:(uint32_t)len
{
    if(!len) return nil;
    NSData *data = [_data subdataWithRange:NSMakeRange(_poz, len)];
    _poz += len;
    return data;
}

- (NSData*)readPrefixedBytes
{
    uint32_t len = [self readUInt32];
    return [self readBytes:len];
}

- (int32_t)readInt32
{
    char *ptr = _pointer + _poz;
    _poz += sizeof(int32_t);
    return *(int32_t*)ptr;
}

- (int64_t)readInt64
{
    char *ptr = _pointer + _poz;
    _poz += sizeof(int64_t);
    return *(int64_t*)ptr;
}

- (int16_t)readInt16
{
    char *ptr = _pointer + _poz;
    _poz += sizeof(int16_t);
    return *(int16_t*)ptr;
}

- (uint32_t)readUInt32
{
    char *ptr = _pointer + _poz;
    _poz += sizeof(uint32_t);
    return *(uint32_t*)ptr;
}

- (uint64_t)readUInt64
{
    char *ptr = _pointer + _poz;
    _poz += sizeof(uint64_t);
    return *(uint64_t*)ptr;
}

- (uint16_t)readUInt16
{
    char *ptr = _pointer + _poz;
    _poz += sizeof(uint16_t);
    return *(uint16_t*)ptr;
}

- (char)readByte
{
    char *ptr = _pointer + _poz;
    _poz += sizeof(char);
    return *(char*)ptr;
}

- (BOOL)readBool
{
    char *ptr = _pointer + _poz;
    _poz += sizeof(BOOL);
    return *(BOOL*)ptr;
}

- (float)readFloat
{
    char *ptr = _pointer + _poz;
    _poz += sizeof(float);
    return *(float*)ptr;
}

- (double)readDouble
{
    char *ptr = _pointer + _poz;
    _poz += sizeof(double);
    return *(double*)ptr;
}

- (NSString*)readString
{
    NSData *data = [self readPrefixedBytes];
    return [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
}

@end
