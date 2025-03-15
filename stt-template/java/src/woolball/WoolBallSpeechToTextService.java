package woolball;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.StringJoiner;
import java.util.concurrent.Flow.Subscriber;
import java.util.function.Supplier;

public class WoolBallSpeechToTextService {
	
	static private HttpClient client = HttpClient.newHttpClient();
	static private URI woolBallUrl = URI.create("https://api.woolball.xyz/v1/speech-to-text");
	//static private URI woolBallUrl = URI.create("http://localhost:8080"); //TODO: remove me
	
	public static String INSECURE_API_KEY = "{{API_KEY}}";
	
	private String apiKey;

	public WoolBallSpeechToTextService(String apiKey) {
		this.apiKey = apiKey;
	}
	
	/**
	 * Transcribes the provided audio file to text using the specified content type and transcription options.
	 *
	 * @param audioData   The path to the audio file to be transcribed.
	 * @param contentType The content type of the audio file.
	 * @param options     Additional transcription options to customize the output.
	 * @return JSON response of the server as a String.
	 * @throws IOException      If an I/O error occurs.
	 * @throws InterruptedException If the operation is interrupted.
	 */
	public String transcribe(Path audioData, String contentType, TranscribeOptions options) throws IOException, InterruptedException {
		var part = new FilePart("audio", audioData);
    	return transcribe(part, options);
    }
	
	/**
	 * Transcribes audio from the specified URL using the provided transcription options.
	 *
	 * @param audioUrl  The URI of the audio file to be transcribed.
	 * @param options   Additional transcription options to customize the output.
	 * @return JSON response of the server as a String.
	 * @throws IOException      If an I/O error occurs.
	 * @throws InterruptedException If the operation is interrupted.
	 */
    public String transcribe(URI audioUrl, TranscribeOptions options) throws IOException, InterruptedException {
    	var part = new StringPart("url", audioUrl.toString(), StandardCharsets.UTF_8);
    	return transcribe(part, options);
    }
    
    String transcribe(Part aditionalData, TranscribeOptions options) throws IOException, InterruptedException {
    	var bodyForm = new MultipartFormDataBodyPublisher()
						.add("model", options.model)
						.add("outputLanguage", options.language)
						.add("returnTimestamps", options.returnTimestamps ? "true" : "false")
						.add("webvtt", options.webvtt ? "true" : "false")
						.add(aditionalData);

        HttpRequest request = HttpRequest.newBuilder(woolBallUrl)
                                        .header("Authorddization", "Bearer " + apiKey)
                                        .header("Content-Type", bodyForm.contentType())
                                        .method("POST", bodyForm)
                                        .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP error! status: " + response.statusCode() + response.body());
        }

        return response.body();
    }
	
	public static class TranscribeOptions {
	    private String model = "onnx-community/whisper-large-v3-turbo_timestamped";
	    private String language = "pt";
	    private boolean returnTimestamps = false;
	    private boolean webvtt = false;

        public TranscribeOptions withModel(String model) {
            this.model = model;
            return this;
        }

        public TranscribeOptions withLanguage(String language) {
        	this.language = language;
            return this;
        }

        public TranscribeOptions withWebvtt(boolean webvtt) {
        	this.webvtt = webvtt;
            return this;
        }
        
        public TranscribeOptions withTimestamps(boolean returnTimestamps) {
        	this.returnTimestamps = returnTimestamps;
            return this;
        }
	}
}

/**
 * Source code from: https://github.com/yskszk63/jnhttp-multipartformdata-bodypublisher
 * dual licensed as MIT and Apache-2.0
 * 
 * multipart/form-data BodyPublisher.
 */
class MultipartFormDataBodyPublisher implements BodyPublisher {
    private static String nextBoundary() {
        var random = new BigInteger(128, new Random());
        try (var formatter = new Formatter()) {
            return formatter.format("-----------------------------%039d", random).toString();
        }
    }

    private final String boundary = nextBoundary();
    private final List<Part> parts = new ArrayList<>();
    private Charset charset;
    private final BodyPublisher delegate = BodyPublishers.ofInputStream(
            () -> Channels.newInputStream(new MultipartFormDataChannel(this.boundary, this.parts, this.charset)));

    /**
     * Construct {@link MultipartFormDataBodyPublisher}
     */
    public MultipartFormDataBodyPublisher() {
        this(Charset.forName("utf8"));
    }

    /*
     * Construct {@link MultipartFormDataBodyPublisher}
     *
     * @param charset
     *            character encoding
     */
    public MultipartFormDataBodyPublisher(Charset charset) {
        this.charset = charset;
    }

    MultipartFormDataBodyPublisher add(Part part) {
        this.parts.add(part);
        return this;
    }

    /**
     * Add part.
     *
     * @param name
     *            field name
     * @param value
     *            field value
     * @return this
     */
    public MultipartFormDataBodyPublisher add(String name, String value) {
        return this.add(new StringPart(name, value, this.charset));
    }

    /**
     * Add part. Content using specified path.
     *
     * @param name
     *            field name
     * @param path
     *            field value
     * @return this
     */
    public MultipartFormDataBodyPublisher addFile(String name, Path path) {
        return this.add(new FilePart(name, path));
    }

    /**
     * Add part. Content using specified path.
     *
     * @param name
     *            field name
     * @param path
     *            field value
     * @param contentType
     *            Content-Type
     * @return this
     */
    public MultipartFormDataBodyPublisher addFile(String name, Path path, String contentType) {
        return this.add(new FilePart(name, path, contentType));
    }

    /**
     * Add part with {@link InputStream}
     *
     * @param name
     *            field name
     * @param filename
     *            file name
     * @param supplier
     *            field value
     * @return this
     */
    public MultipartFormDataBodyPublisher addStream(String name, String filename, Supplier<InputStream> supplier) {
        return this.add(new StreamPart(name, filename, () -> Channels.newChannel(supplier.get())));
    }

    /**
     * Add part with {@link InputStream}
     *
     * @param name
     *            field name
     * @param filename
     *            file name
     * @param supplier
     *            field value
     * @param contentType
     *            Content-Type
     * @return this
     */
    public MultipartFormDataBodyPublisher addStream(String name, String filename, Supplier<InputStream> supplier,
            String contentType) {
        return this.add(new StreamPart(name, filename, () -> Channels.newChannel(supplier.get()), contentType));
    }

    /**
     * Add part with {@link ReadableByteChannel}
     *
     * @param name
     *            field name
     * @param filename
     *            file name
     * @param supplier
     *            field value
     * @return this
     */
    public MultipartFormDataBodyPublisher addChannel(String name, String filename,
            Supplier<ReadableByteChannel> supplier) {
        return this.add(new StreamPart(name, filename, supplier));
    }

    /**
     * Add part with {@link ReadableByteChannel}
     *
     * @param name
     *            field name
     * @param filename
     *            file name
     * @param supplier
     *            field value
     * @param contentType
     *            Content-Type
     * @return this
     */
    public MultipartFormDataBodyPublisher addChannel(String name, String filename,
            Supplier<ReadableByteChannel> supplier, String contentType) {
        return this.add(new StreamPart(name, filename, supplier, contentType));
    }

    /**
     * Get Content-Type
     *
     * @return Content-Type
     */
    public String contentType() {
        try (var formatter = new Formatter()) {
            return formatter.format("multipart/form-data; boundary=%s", this.boundary).toString();
        }
    }

    @Override
    public void subscribe(Subscriber<? super ByteBuffer> s) {
        delegate.subscribe(s);
    }

    @Override
    public long contentLength() {
        return delegate.contentLength();
    }

}

interface Part {
    String name();

    default Optional<String> filename() {
        return Optional.empty();
    }

    default Optional<String> contentType() {
        return Optional.empty();
    }

    ReadableByteChannel open() throws IOException;
}

class StringPart implements Part {
    private final String name;
    private final String value;
    private final Charset charset;

    StringPart(String name, String value, Charset charset) {
        this.name = name;
        this.value = value;
        this.charset = charset;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public ReadableByteChannel open() throws IOException {
        var input = new ByteArrayInputStream(this.value.getBytes(this.charset));
        return Channels.newChannel(input);
    }
}

class StreamPart implements Part {
    private final String name;
    private final String filename;
    private final Supplier<ReadableByteChannel> supplier;
    private final String contentType;

    StreamPart(String name, String filename, Supplier<ReadableByteChannel> supplier, String contentType) {
        this.name = name;
        this.filename = filename;
        this.supplier = supplier;
        this.contentType = contentType;
    }

    StreamPart(String name, String filename, Supplier<ReadableByteChannel> supplier) {
        this(name, filename, supplier, "application/octet-stream");
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Optional<String> filename() {
        return Optional.of(filename);
    }

    @Override
    public Optional<String> contentType() {
        return Optional.of(this.contentType);
    }

    @Override
    public ReadableByteChannel open() throws IOException {
        return this.supplier.get();
    }
}

class FilePart implements Part {
    private final String name;
    private final Path path;
    private final String contentType;

    FilePart(String name, Path path, String contentType) {
        this.name = name;
        this.path = path;
        this.contentType = contentType;
    }

    FilePart(String name, Path path) {
        this(name, path, "application/octet-stream");
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Optional<String> filename() {
        return Optional.of(this.path.getFileName().toString());
    }

    @Override
    public Optional<String> contentType() {
        return Optional.of(this.contentType);
    }

    @Override
    public ReadableByteChannel open() throws IOException {
        return Files.newByteChannel(this.path);
    }
}

enum State {
    Boundary, Headers, Body, Done,
}

class MultipartFormDataChannel implements ReadableByteChannel {
    private static final Charset LATIN1 = Charset.forName("ISO-8859-1");
    private boolean closed = false;
    private State state = State.Boundary;
    private final String boundary;
    private final Iterator<Part> parts;
    private ByteBuffer buf = ByteBuffer.allocate(0);
    private Part current = null;
    private ReadableByteChannel channel = null;
    private final Charset charset;

    MultipartFormDataChannel(String boundary, Iterable<Part> parts, Charset charset) {
        this.boundary = boundary;
        this.parts = parts.iterator();
        this.charset = charset;
    }

    @Override
    public void close() throws IOException {
        if (this.channel != null) {
            this.channel.close();
            this.channel = null;
        }
        this.closed = true;
    }

    @Override
    public boolean isOpen() {
        return !this.closed;
    }

    @Override
    public int read(ByteBuffer buf) throws IOException {
        while (true) {
            if (this.buf.hasRemaining()) {
                var n = Math.min(this.buf.remaining(), buf.remaining());
                var slice = this.buf.slice();
                slice.limit(n);
                buf.put(slice);
                this.buf.position(this.buf.position() + n);
                return n;
            }

            switch (this.state) {
            case Boundary:
                if (this.parts.hasNext()) {
                    this.current = this.parts.next();
                    this.buf = ByteBuffer.wrap(("--" + this.boundary + "\r\n").getBytes(LATIN1));
                    this.state = State.Headers;
                } else {
                    this.buf = ByteBuffer.wrap(("--" + this.boundary + "--\r\n").getBytes(LATIN1));
                    this.state = State.Done;
                }
                break;

            case Headers:
                this.buf = ByteBuffer.wrap(this.currentHeaders().getBytes(this.charset));
                this.state = State.Body;
                break;

            case Body:
                if (this.channel == null) {
                    this.channel = this.current.open();
                }

                var n = this.channel.read(buf);
                if (n == -1) {
                    this.channel.close();
                    this.channel = null;
                    this.buf = ByteBuffer.wrap("\r\n".getBytes(LATIN1));
                    this.state = State.Boundary;
                } else {
                    return n;
                }
                break;

            case Done:
                return -1;
            }
        }
    }

    static String escape(String s) {
        return s.replaceAll("\"", "\\\"");
    }

    String currentHeaders() {
        var current = this.current;

        if (current == null) {
            throw new IllegalStateException();
        }

        var contentType = current.contentType();
        var filename = current.filename();
        if (contentType.isPresent() && filename.isPresent()) {
            var format = new StringJoiner("\r\n", "", "\r\n")
                    .add("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"").add("Content-Type: %s")
                    .toString();
            try (var formatter = new Formatter()) {
                return formatter
                        .format(format, escape(current.name()), escape(filename.get()), escape(contentType.get()))
                        .toString() + "\r\n"; // FIXME
            }

        } else if (contentType.isPresent()) {
            var format = new StringJoiner("\r\n", "", "\r\n").add("Content-Disposition: form-data; name=\"%s\"")
                    .add("Content-Type: %s").toString();
            try (var formatter = new Formatter()) {
                return formatter.format(format, escape(current.name()), escape(contentType.get())).toString() + "\r\n"; // FIXME
                                                                                                                        // escape
            }

        } else if (filename.isPresent()) {
            var format = new StringJoiner("\r\n", "", "\r\n")
                    .add("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"").toString();
            try (var formatter = new Formatter()) {
                return formatter.format(format, escape(current.name()), escape(filename.get())).toString() + "\r\n"; // FIXME
                                                                                                                     // escape
            }

        } else {
            var format = new StringJoiner("\r\n", "", "\r\n").add("Content-Disposition: form-data; name=\"%s\"")
                    .toString();
            try (var formatter = new Formatter()) {
                return formatter.format(format, escape(current.name())).toString() + "\r\n"; // FIXME escape
            }
        }
    }
}
