package com.yun.baselibrary.http;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.platform.Platform;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

import static okhttp3.internal.platform.Platform.INFO;

/**
 * An OkHttp interceptor which logs request and response information. Can be applied as an
 * {@linkplain OkHttpClient#interceptors() application interceptor} or as a {@linkplain
 * OkHttpClient#networkInterceptors() network interceptor}. <p> The format of the logs created by
 * this class should not be considered stable and may change slightly between releases. If you need
 * a stable logging format, use your own interceptor.
 */
public final class HttpLoggingInterceptor2 implements Interceptor {
  private static final Charset UTF8 = Charset.forName("UTF-8");

  public enum Level {
    /** No logs. */
    NONE,
    /**
     * Logs request and response lines.
     *
     * <p>Example:
     * <pre>{@code
     * --> POST /greeting http/1.1 (3-byte body)
     *
     * <-- 200 OK (22ms, 6-byte body)
     * }</pre>
     */
    BASIC,
    /**
     * Logs request and response lines and their respective headers.
     *
     * <p>Example:
     * <pre>{@code
     * --> POST /greeting http/1.1
     * Host: example.com
     * Content-Type: plain/text
     * Content-Length: 3
     * --> END POST
     *
     * <-- 200 OK (22ms)
     * Content-Type: plain/text
     * Content-Length: 6
     * <-- END HTTP
     * }</pre>
     */
    HEADERS,
    /**
     * Logs request and response lines and their respective headers and bodies (if present).
     *
     * <p>Example:
     * <pre>{@code
     * --> POST /greeting http/1.1
     * Host: example.com
     * Content-Type: plain/text
     * Content-Length: 3
     *
     * Hi?
     * --> END POST
     *
     * <-- 200 OK (22ms)
     * Content-Type: plain/text
     * Content-Length: 6
     *
     * Hello!
     * <-- END HTTP
     * }</pre>
     */
    BODY
  }

  public interface Logger {
    void log(String message);

    /** A {@link Logger} defaults output appropriate for the current platform. */
    Logger DEFAULT = new Logger() {
      @Override public void log(String message) {
        Platform.get().log(INFO, message, null);
      }
    };
  }

  public HttpLoggingInterceptor2() {
    this(Logger.DEFAULT);
  }

  public HttpLoggingInterceptor2(Logger logger) {
    this.logger = logger;
  }

  private final Logger logger;

  private volatile Level level = Level.NONE;

  /** Change the level at which this interceptor logs. */
  public HttpLoggingInterceptor2 setLevel(Level level) {
    if (level == null) throw new NullPointerException("level == null. Use Level.NONE instead.");
    this.level = level;
    return this;
  }

  public Level getLevel() {
    return level;
  }

  @Override public Response intercept(Chain chain) throws IOException {
    Level level = this.level;

    Request request = chain.request();
    if (level == Level.NONE) {
      return chain.proceed(request);
    }

    boolean logBody = level == Level.BODY;
    boolean logHeaders = logBody || level == Level.HEADERS;

    RequestBody requestBody = request.body();
    boolean hasRequestBody = requestBody != null;

    Connection connection = chain.connection();
    StringBuilder sb = new StringBuilder();
    String requestStartMessage = "--> "
        + request.method()
        + ' ' + request.url()
        + (connection != null ? " " + connection.protocol() : "");
    if (!logHeaders && hasRequestBody) {
      requestStartMessage += " (" + requestBody.contentLength() + "-byte body)";
    }
    sb.append(requestStartMessage+" \n");
//    logger.log(requestStartMessage);

    if (logHeaders) {
      if (hasRequestBody) {
        // Request body headers are only present when installed as a network interceptor. Force
        // them to be included (when available) so there values are known.
        if (requestBody.contentType() != null) {
          sb.append("Content-Type: " + requestBody.contentType()+"\n");
//          logger.log("Content-Type: " + requestBody.contentType());
        }
        if (requestBody.contentLength() != -1) {
          sb.append("Content-Length: " + requestBody.contentLength()+"\n");
//          logger.log("Content-Length: " + requestBody.contentLength());
        }
      }

      Headers headers = request.headers();
      for (int i = 0, count = headers.size(); i < count; i++) {
        String name = headers.name(i);
        // Skip headers from the request body as they are explicitly logged above.
        if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
//          logger.log(name + ": " + headers.value(i));
          sb.append(name + ": " + headers.value(i)+"\n");
        }
      }

      if (!logBody || !hasRequestBody) {
//        logger.log("--> END " + request.method());
        sb.append("--> END " + request.method()+"\n");
      } else if (bodyHasUnknownEncoding(request.headers())) {
        sb.append("--> END " + request.method() + " (encoded body omitted)"+"\n");
//        logger.log("--> END " + request.method() + " (encoded body omitted)");
      } else {
        Buffer buffer = new Buffer();
        requestBody.writeTo(buffer);

        Charset charset = UTF8;
        MediaType contentType = requestBody.contentType();
        if (contentType != null) {
          charset = contentType.charset(UTF8);
        }

        sb.append("\n");
//        logger.log("");
        if (isPlaintext(buffer)) {
          sb.append(buffer.readString(charset)+"\n");
          sb.append("--> END " + request.method()
                  + " (" + requestBody.contentLength() + "-byte body)"+"\n");
//          logger.log(buffer.readString(charset));
//          logger.log("--> END " + request.method()
//              + " (" + requestBody.contentLength() + "-byte body)");
        } else {
          sb.append("--> END " + request.method() + " (binary "
                  + requestBody.contentLength() + "-byte body omitted)"+"\n");
//          logger.log("--> END " + request.method() + " (binary "
//              + requestBody.contentLength() + "-byte body omitted)");
        }
      }
    }
    com.orhanobut.logger.Logger.d(sb.toString());
    StringBuilder sbr = new StringBuilder();
    long startNs = System.nanoTime();
    Response response;
    try {
      response = chain.proceed(request);
    } catch (Exception e) {
      sbr.append("<-- HTTP FAILED: " + e+"\n");
//      logger.log("<-- HTTP FAILED: " + e);
      throw e;
    }
    long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

    ResponseBody responseBody = response.body();
    long contentLength = responseBody.contentLength();
    String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
    sbr.append("<-- "
            + response.code()
            + (response.message().isEmpty() ? "" : ' ' + response.message())
            + ' ' + response.request().url()
            + " (" + tookMs + "ms" + (!logHeaders ? ", " + bodySize + " body" : "") + ')'+"\n");
//    logger.log("<-- "
//        + response.code()
//        + (response.message().isEmpty() ? "" : ' ' + response.message())
//        + ' ' + response.request().url()
//        + " (" + tookMs + "ms" + (!logHeaders ? ", " + bodySize + " body" : "") + ')');

    if (logHeaders) {
      Headers headers = response.headers();
      for (int i = 0, count = headers.size(); i < count; i++) {
//        logger.log(headers.name(i) + ": " + headers.value(i));
        sbr.append(headers.name(i) + ": " + headers.value(i)+"\n");
      }

      if (!logBody || !HttpHeaders.hasBody(response)) {
//        logger.log("<-- END HTTP");
        sbr.append("<-- END HTTP"+"\n");
      } else if (bodyHasUnknownEncoding(response.headers())) {
        sbr.append("<-- END HTTP (encoded body omitted)"+"\n");
//        logger.log("<-- END HTTP (encoded body omitted)");
      } else {
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.buffer();

        Long gzippedLength = null;
        if ("gzip".equalsIgnoreCase(headers.get("Content-Encoding"))) {
          gzippedLength = buffer.size();
          GzipSource gzippedResponseBody = null;
          try {
            gzippedResponseBody = new GzipSource(buffer.clone());
            buffer = new Buffer();
            buffer.writeAll(gzippedResponseBody);
          } finally {
            if (gzippedResponseBody != null) {
              gzippedResponseBody.close();
            }
          }
        }

        Charset charset = UTF8;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
          charset = contentType.charset(UTF8);
        }

        if (!isPlaintext(buffer)) {
//          logger.log("");
          sbr.append("<-- END HTTP (binary " + buffer.size() + "-byte body omitted)"+"\n");

//          logger.log("<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");
          return response;
        }

        if (contentLength != 0) {
//          logger.log("");
//          logger.log(buffer.clone().readString(charset));
          sbr.append(unicodeToUTF_8(buffer.clone().readString(charset))+"\n");

        }

        if (gzippedLength != null) {
          sbr.append("<-- END HTTP (" + buffer.size() + "-byte, "
                  + gzippedLength + "-gzipped-byte body)"+"\n");
//            logger.log("<-- END HTTP (" + buffer.size() + "-byte, "
//                + gzippedLength + "-gzipped-byte body)");
        } else {
          sbr.append("<-- END HTTP (" + buffer.size() + "-byte body)");
//            logger.log("<-- END HTTP (" + buffer.size() + "-byte body)");
        }
      }
    }
    com.orhanobut.logger.Logger.d(sbr.toString());
    sb = null;
    sbr= null;
    return response;
  }

  public  String unicodeToUTF_8(String src) {
    if (null == src) {
      return null;
    }
//    System.out.println("src: " + src);
    StringBuilder out = new StringBuilder();
    for (int i = 0; i < src.length(); ) {
      char c = src.charAt(i);
      if (i + 6 < src.length() && c == '\\' && src.charAt(i + 1) == 'u') {
        String hex = src.substring(i + 2, i + 6);
        try {
          out.append((char) Integer.parseInt(hex, 16));
        } catch (NumberFormatException nfe) {
          nfe.fillInStackTrace();
        }
        i = i + 6;
      } else {
        out.append(src.charAt(i));
        ++i;
      }
    }
    return out.toString();
  }
  /**
   * Returns true if the body in question probably contains human readable text. Uses a small sample
   * of code points to detect unicode control characters commonly used in binary file signatures.
   */
  static boolean isPlaintext(Buffer buffer) {
    try {
      Buffer prefix = new Buffer();
      long byteCount = buffer.size() < 64 ? buffer.size() : 64;
      buffer.copyTo(prefix, 0, byteCount);
      for (int i = 0; i < 16; i++) {
        if (prefix.exhausted()) {
          break;
        }
        int codePoint = prefix.readUtf8CodePoint();
        if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
          return false;
        }
      }
      return true;
    } catch (EOFException e) {
      return false; // Truncated UTF-8 sequence.
    }
  }

  private boolean bodyHasUnknownEncoding(Headers headers) {
    String contentEncoding = headers.get("Content-Encoding");
    return contentEncoding != null
        && !contentEncoding.equalsIgnoreCase("identity")
        && !contentEncoding.equalsIgnoreCase("gzip");
  }
}
