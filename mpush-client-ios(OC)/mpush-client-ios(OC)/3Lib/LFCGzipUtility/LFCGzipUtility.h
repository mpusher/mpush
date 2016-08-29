#import <Foundation/Foundation.h>
#import "zlib.h"
 
@interface LFCGzipUtility : NSObject
{
 
}
 
+(NSData*) gzipData:(NSData*)pUncompressedData;  //压缩
+(NSData*) ungzipData:(NSData *)compressedData;  //解压缩
 
@end