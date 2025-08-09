package io.github.jasonlat.types.exception.file;


import io.github.jasonlat.types.exception.BaseException;

/**
 * 文件信息异常类
 * 
 * @author ruoyi
 */
public class FileException extends BaseException {

    private static final long serialVersionUID = 1L;

    public FileException(String code, Object[] args)
    {
        super("file", code, args, null);
    }

}
