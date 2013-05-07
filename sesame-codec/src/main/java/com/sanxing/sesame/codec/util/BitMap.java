package com.sanxing.sesame.codec.util;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class BitMap
{
    private static BitSet decodeBitMap( ByteBuffer buf, int bitNum )
    {
        BitSet bs = new BitSet( bitNum );
        int pos = 0;
        byte[] temp = new byte[bitNum / 8];
        buf.get( temp );
        for ( byte b : temp )
        {
            for ( int i = 0; i < 8; ++i )
            {
                int bl = 0;
                bl = b >>> 7 - i & 0x1;
                if ( bl == 1 )
                {
                    bs.set( pos );
                }
                ++pos;
            }
        }
        return bs;
    }

    public static BitSet getBitset( ByteBuffer recvBuf )
    {
        byte firstMap = recvBuf.get();
        recvBuf.position( recvBuf.position() - 1 );
        if ( ( ( firstMap & 0xFF ) >> 7 & 0x1 ) == 0 )
        {
            return decodeBitMap( recvBuf, 64 );
        }
        return decodeBitMap( recvBuf, 128 );
    }

    public byte[] encodeBitMap( BitSet bs )
    {
        int byteNum = bs.size() / 8;
        byte[] result = new byte[byteNum];
        int pos = 0;
        for ( int i = 0; i < byteNum; ++i )
        {
            int value = 0;
            if ( bs.get( pos++ ) )
            {
                value += 128;
            }
            if ( bs.get( pos++ ) )
            {
                value += 64;
            }
            if ( bs.get( pos++ ) )
            {
                value += 32;
            }
            if ( bs.get( pos++ ) )
            {
                value += 16;
            }
            if ( bs.get( pos++ ) )
            {
                value += 8;
            }
            if ( bs.get( pos++ ) )
            {
                value += 4;
            }
            if ( bs.get( pos++ ) )
            {
                value += 2;
            }
            if ( bs.get( pos++ ) )
            {
                ++value;
            }

            result[i] = (byte) value;
        }

        return result;
    }
}