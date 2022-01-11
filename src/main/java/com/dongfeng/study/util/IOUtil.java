package com.dongfeng.study.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.dongfeng.study.bean.base.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author eastFeng
 * @date 2020/8/15 - 14:27
 */
@Slf4j
public class IOUtil {
    /**
     * 4096、8192都可以
     */
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    /**
     * 数据流末尾
     */
    public static final int EOF = -1;

    public static void main(String[] args) {
        // IO操作，推按使用hutool或者其他工具类
        /**
         * {@link cn.hutool.core.io.IoUtil} 、{@link cn.hutool.core.io.FileUtil}
         */

        try {
            File file = new File("D:\\Wstudy\\Test1.txt");
//            InputStream inputStream = Files.newInputStream(file.toPath());
//            IoUtil.read(inputStream, StandardCharsets.UTF_8);

            String str = readFile(file);
            System.out.println(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Response<String> uploadImage(MultipartFile file){
        try {
            InputStream inputStream = file.getInputStream();
            File tempFile = File.createTempFile("hhhhh", ".jpg");
            copyInputStreamToFile(inputStream, tempFile, false, true);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return Response.successInstance("success");
    }

    /* ---------------- writeString -------------- */

    /**
     * 将String写入文件，追加模式，字符集（编码方式）为UTF-8
     * @param data 写入的内容
     * @param file 文件
     * @throws IOException IO异常
     */
    public static void appendString(String data, File file) throws IOException{
        writeString(data, file, StandardCharsets.UTF_8, true);
    }

    /**
     * 将String写入文件，追加模式
     * @param data 写入的内容
     * @param file 文件
     * @param charset 字符集（编码方式）
     * @throws IOException IO异常
     */
    public static void appendString(String data, File file, Charset charset) throws IOException{
        writeString(data, file, charset, true);
    }


    /**
     * 将String写入文件，覆盖模式，字符集（编码方式）为UTF-8
     * @param data 写入的内容
     * @param file 文件
     * @throws IOException IO异常
     */
    public static void writeUtf8String(String data, File file) throws IOException{
        writeString(data, file, StandardCharsets.UTF_8, false);
    }

    /**
     * 将String写入文件，覆盖模式
     * @param data 写入的内容
     * @param file 文件
     * @param charset 字符集（编码方式）
     * @throws IOException IO异常
     */
    public static void writeString(String data, File file, Charset charset) throws IOException{
        writeString(data, file, charset, false);
    }

    /**
     * 将String写入文件
     * @param data 写入的内容
     * @param file 文件
     * @param charset 字符集（编码方式）
     * @param isAppend  是否追加
     * @throws IOException IO异常
     */
    public static void writeString(String data, File file, Charset charset, boolean isAppend) throws IOException{
        if (StringUtils.isBlank(data) || file==null || charset==null){
            return;
        }

        BufferedWriter bufferedWriter = null;
        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, isAppend), charset);
            bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(data);
            bufferedWriter.flush();
        } finally {
            close(bufferedWriter);
        }
    }

    /* ---------------- readLines -------------- */
    public static List<String> readLines(File file, Charset charset) throws IOException{
        List<String> list = new ArrayList<>();
        if (file!=null || charset!=null){
            readLines(file, charset, list);
        }
        return list;
    }

    public static void readLines(File file, Charset charset, Collection<String> collection) throws IOException {
        if (file==null || charset==null || collection==null){
            return;
        }

        BufferedReader bufferedReader = null;
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), charset);
            bufferedReader = new BufferedReader(inputStreamReader);

            while (true){
                String line = bufferedReader.readLine();
                if (line == null){
                    break;
                }
                collection.add(line);
            }
        } finally {
            close(bufferedReader);
        }

    }


    /* ---------------- 从 -------------- */
    /**
     * 从文件中读取字符串
     * @param file 文件
     * @return String 字符串
     * @throws IOException io异常
     */
    public static String readFile(File file) throws IOException {
        Objects.requireNonNull(file, "File is null");

        // 输入流：输入流和数据源相关，从数据源中读取数据
        InputStream in = null;
        // ByteArrayOutputStream：将数据写入到动态的字节数组中
        ByteArrayOutputStream out = null;
        try {
            in = new FileInputStream(file);
            out = new ByteArrayOutputStream();
            copy(in, out, false, false);
            return out.toString();
        } finally {
            // 在这里关闭就行
            close(in);
            close(out);
        }
    }

    /**
     * 将文件全部内容读入到一个字符串
     *
     * @param file 文件
     * @param charset 编码类型
     * @return 文件中的字符串
     * @throws IOException 没找到文件
     */
    public static String readFile(File file, Charset charset) throws IOException {
        Objects.requireNonNull(file, "File is null");

        InputStreamReader reader = null;
        StringWriter writer = new StringWriter();
        try {
            if (charset == null){
                reader = new InputStreamReader(new FileInputStream(file));
            }else {
                reader = new InputStreamReader(new FileInputStream(file), charset);
            }
            copy(reader, writer);
        } finally {
            close(reader);
            close(writer);
        }

        return writer.toString();
    }

    /**
     * 从{@link Reader}中读取String
     *
     * @param reader 字符输入流
     * @param isClose 是否关闭输入流
     * @return String
     * @throws IOException IO异常
     */
    public static String read(Reader reader, boolean isClose) throws IOException {
        Objects.requireNonNull(reader, "Reader is null");

        final StringBuilder builder = new StringBuilder();
        final CharBuffer buffer = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);

        try {
            while (-1 != reader.read(buffer)) {
                builder.append(buffer.flip());
            }
        }finally {
            if (isClose) {
                close(reader);
            }
        }
        return builder.toString();
    }

    /**
     * 从字节输入流中读取内容，读取完毕后关闭流
     *
     * @param inputStream 字节输入流
     * @param charset 字符集
     * @return 内容（字符串）
     */
    public static String read(InputStream inputStream, Charset charset) throws IOException {
        Objects.requireNonNull(inputStream, "InputStream is null");
        Objects.requireNonNull(charset, "Charset is null");

        return new String(readBytes(inputStream, true), charset);
    }

    /**
     * 从字节输入流中读取bytes（字节数组）
     *
     * @param input 字节输入流
     * @return 字节数组
     */
    public static byte[] readBytes(InputStream input, boolean isClose) throws IOException {
        // 判断字节流是否是文件字节流
        // 文件字节流的长度是可预见的，此时直接读取效率更高
        if (input instanceof FileInputStream){
            int available = input.available();
            final byte[] result = new byte[available];
            int readLength = input.read(result);
            if (readLength != available) {
                throw new IOException(StrUtil.format("File length is [{}] but read [{}]!", available, readLength));
            }
            return result;
        }
        // 字节流是不是文件字节流
        return read(input, isClose).toByteArray();
    }

    /**
     * 从流中读取内容，读到输出流中，读取完毕后可选是否关闭输入流
     *
     * @param inputStream 输入流
     * @param isClose 是否关闭输入流
     * @return 输出流
     * @throws IOException 如果发生IO异常
     */
    public static ByteArrayOutputStream read(InputStream inputStream, boolean isClose) throws IOException {
        ByteArrayOutputStream outputStream = null;
        // 判断输入流是否是文件字节流
        if (inputStream instanceof FileInputStream){
            outputStream = new ByteArrayOutputStream(inputStream.available());
        }else {
            outputStream = new ByteArrayOutputStream();
        }

        copy(inputStream, outputStream, isClose, false);
        return outputStream;
    }

    // --------------------------------------- 字节流拷贝 start --------------------------------------
    /**
     * 将流的内容写入文件
     * @param source 输入流
     * @param destination 目标文件
     * @param append 是否以追加的方式写入到文件，true是追加，false是覆盖
     * @param isCloseIn 是否关闭输入流
     * @return 传输（拷贝）的byte（字节）数
     */
    public static long copyInputStreamToFile(InputStream source, File destination, boolean append, boolean isCloseIn) throws IOException {
        Objects.requireNonNull(source, "source is null");
        Objects.requireNonNull(destination, "destination File is null");

        // 文件输出流
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(destination, append);
            // 拷贝输入流的内容到输出流
            return copy(source, output, isCloseIn, false);
        } finally {
            // 在这里关闭文件输出流就行
            close(output);
        }
    }

    /**
     * 拷贝输入流的内容到输出流，拷贝后关闭流，
     *
     * @param input 输入流
     * @param output 输出流
     * @return 传输的byte数
     */
    public static long copy(InputStream input, OutputStream output) throws IOException{
        return copy(input, output, DEFAULT_BUFFER_SIZE, true, true);
    }

    /**
     * 拷贝输入流的内容到输出流
     *
     * @param input 输入流
     * @param output 输出流
     * @param isCloseIn 是否关闭输入流
     * @param isCloseOut 是否关闭输出流
     * @return 传输的字节数
     * @throws IOException
     */
    public static long copy(InputStream input, OutputStream output,
                            boolean isCloseIn, boolean isCloseOut) throws IOException{
        return copy(input, output, DEFAULT_BUFFER_SIZE, isCloseIn, isCloseOut);
    }

    /**
     * 拷贝输入流的内容到输出流，拷贝后不关闭流
     *
     * @param input 输入流
     * @param output 输出流
     * @param bufferSize 数组大小（缓存大小）
     * @param isCloseIn 是否关闭输入流
     * @param isCloseOut 是否关闭输出流
     * @return 传输（拷贝）的byte（字节）数
     */
    public static long copy(InputStream input, OutputStream output, int bufferSize,
                            boolean isCloseIn, boolean isCloseOut) throws IOException {
        Objects.requireNonNull(input, "InputStream is null!");
        Objects.requireNonNull(output, "OutputStream is null");
        if (bufferSize <= 0){
            bufferSize = DEFAULT_BUFFER_SIZE;
        }

        // 总共拷贝的字节数量
        long byteCount = 0;
        // 从输入流中读取的字节放入buf数组中，一次最多读取bufferSize个字节
        byte[] buf = new byte[bufferSize];
        // 每次从输入流中读取的字节数量
        int readBytes;
        try {
            while ((readBytes=input.read(buf)) != EOF){
                // 将充输入流中读取到的数据（字节）写入到输出流中
                output.write(buf, 0, readBytes);
                byteCount += readBytes;
            }
            output.flush();
        } finally {
            if (isCloseIn){
                close(input);
            }
            if (isCloseOut){
                close(output);
            }
        }

        return byteCount;
    }

    /**
     * 拷贝文件流，使用NIO
     *
     * @param input 文件输入流
     * @param output 文件输出流
     * @return 拷贝的字节数
     * @throws IOException 如果发生IO异常
     */
    public static long copy(FileInputStream input, FileOutputStream output) throws IOException{
        Objects.requireNonNull(input, "FileInputStream is null");
        Objects.requireNonNull(output, "FileOutputStream is null");

        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = input.getChannel();
            outChannel = output.getChannel();
            return inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            close(outChannel);
            close(inChannel);
        }
    }
    // --------------------------------- 字节流拷贝 end-------------------------------------


    //------------------------------------------------------------------------------------
    //     字符流（java.io.Reader / java.io.Writer） 拷贝 start
    // -----------------------------------------------------------------------------------
    /**
     * 拷贝输入流的内容到输出流，拷贝后不关闭流，使用默认Buffer大小
     *
     * @param reader 字符输入流
     * @param writer 字符输出流
     * @throws IOException IO异常
     * @return 传输的char数量
     */
    public static long copy(Reader reader, Writer writer) throws IOException{
        return copy(reader, writer, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 拷贝输入流的内容到输出流，拷贝后不关闭流
     *
     * @param reader 字符输入流（源）
     * @param writer 字符输出流（目的地）
     * @throws IOException IO异常
     * @return 传输的char数量
     */
    public static long copy(Reader reader, Writer writer, int bufferSize) throws IOException {
        Objects.requireNonNull(reader, "Reader is null");
        Objects.requireNonNull(writer, "Writer is null");

        if (bufferSize <= 0){
            bufferSize = DEFAULT_BUFFER_SIZE;
        }

        // 总共拷贝的字节数量
        long charCount = 0;
        // 每次读到的字符（char）放入本char数组
        char[] charBuf = new char[bufferSize];
        // 每次读到的字符（char）数
        int readChars;
        while ((readChars=reader.read(charBuf)) != EOF){
            // 从字符输入流中读取的数据写入字符输出流
            writer.write(charBuf, 0, readChars);
            charCount += readChars;
        }
        writer.flush();
        return charCount;
    }
    // --------------------------------- 字符流拷贝 end-------------------------------------


    /**
     * 关闭流
     * <p> 关闭失败不会抛出异常
     */
    public static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                log.error("IO close error:{}", e.getMessage(), e);
            }
        }
    }
}