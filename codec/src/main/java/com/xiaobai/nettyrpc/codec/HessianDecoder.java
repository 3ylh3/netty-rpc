package com.xiaobai.nettyrpc.codec;

import com.caucho.hessian.io.Hessian2Input;
import com.xiaobai.nettyrpc.dto.TransferDTO;

import java.io.ByteArrayInputStream;

/**
 * Hessian decoder
 *
 * @author yinzhaojing
 * @date 2022-06-21 20:09:38
 */
public class HessianDecoder extends AbstractDecoder {

    @Override
    public TransferDTO decode(byte[] bytes) throws Exception {
        Hessian2Input hessian2Input = null;
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            hessian2Input = new Hessian2Input(byteArrayInputStream);
            return (TransferDTO) hessian2Input.readObject();
        } finally {
            if (null != hessian2Input) {
                hessian2Input.close();
            }
        }
    }
}
