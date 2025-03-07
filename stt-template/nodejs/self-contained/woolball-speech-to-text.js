class TranscriptionOptions {
    constructor() {
        this.model = "onnx-community/whisper-large-v3-turbo_timestamped";
        this.language = "pt";
        this.returnTimestamps = false;
        this.webvtt = false;
    }
}

class WoolBallSpeechToTextService {
    constructor() {
        this.baseUrl = "https://api.woolball.xyz/v1";
        this.headers = { 'Authorization': `Bearer {{API_KEY}}` };
    }

    async transcribeFromUrl(audioUrl, options = new TranscriptionOptions()) {
        const formData = new FormData();
        formData.append('url', audioUrl);
        formData.append('model', options.model);
        formData.append('language', options.language);
        formData.append('returnTimestamps', options.returnTimestamps.toString());
        formData.append('webvtt', options.webvtt.toString());
        
        const response = await fetch(`${this.baseUrl}/speech-to-text`, {
            method: 'POST',
            headers: this.headers,
            body: formData
        });

        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    }

    async transcribeFromUrlWithTimestamps(audioUrl, language = "pt") {
        return this.transcribeFromUrl(audioUrl, {
            ...new TranscriptionOptions(),
            language,
            returnTimestamps: true
        });
    }

    async transcribeFromUrlWithWebVtt(audioUrl, language = "pt") {
        return this.transcribeFromUrl(audioUrl, {
            ...new TranscriptionOptions(),
            language,
            returnTimestamps: true,
            webvtt: true
        });
    }

    async transcribeFromFile(audioData, options = new TranscriptionOptions()) {
        const formData = new FormData();
        formData.append('file', new Blob([audioData]), 'audio.mp3');
        formData.append('model', options.model);
        formData.append('language', options.language);
        formData.append('returnTimestamps', options.returnTimestamps.toString());
        formData.append('webvtt', options.webvtt.toString());
        
        const response = await fetch(`${this.baseUrl}/speech-to-text`, {
            method: 'POST',
            headers: this.headers,
            body: formData
        });

        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    }

    async transcribeFromFileWithTimestamps(audioData, language = "pt") {
        return this.transcribeFromFile(audioData, {
            ...new TranscriptionOptions(),
            language,
            returnTimestamps: true
        });
    }

    async transcribeFromFileWithWebVtt(audioData, language = "pt") {
        return this.transcribeFromFile(audioData, {
            ...new TranscriptionOptions(),
            language,
            returnTimestamps: true,
            webvtt: true
        });
    }

    async getAvailableModels() {
        const response = await fetch(`${this.baseUrl}/speech-to-text-models`, {
            headers: this.headers
        });

        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    }
}

export { TranscriptionOptions, WoolBallSpeechToTextService };
