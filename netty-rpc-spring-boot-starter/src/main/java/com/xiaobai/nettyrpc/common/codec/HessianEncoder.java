package com.xiaobai.nettyrpc.common.codec;

import com.caucho.hessian.io.Hessian2Output;
import com.xiaobai.nettyrpc.common.dto.TransferDTO;

import java.io.ByteArrayOutputStream;

/**
 * Hessian encoder
 *
 * @author yinzhaojing
 * @date 2022-06-21 20:02:21
 */
public class HessianEncoder extends AbstractEncoder {

    @Override
    public byte[] encode(TransferDTO msg) throws Exception {

        Hessian2Output hessian2Output = null;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            hessian2Output = new Hessian2Output(byteArrayOutputStream);
            hessian2Output.writeObject(msg);
            hessian2Output.flush();
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } finally {
            if (null != hessian2Output) {
                hessian2Output.close();
            }
        }
    }
}
