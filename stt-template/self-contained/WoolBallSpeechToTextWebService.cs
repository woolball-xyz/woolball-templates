using System;
using System.Net.Http;
using System.Threading.Tasks;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace WoolBall.SpeechToText
{
    public class TranscriptionOptions
    {
        public string Model { get; set; } = "onnx-community/whisper-large-v3-turbo_timestamped";
        public string Language { get; set; } = "pt";
        public bool ReturnTimestamps { get; set; } = false;
        public bool Webvtt { get; set; } = false;
    }

    public class WoolBallSpeechToTextWebService
    {
        private readonly HttpClient _httpClient;
        private const string BaseUrl = "https://api.woolball.xyz/v1";
        private const string API_KEY_PLACEHOLDER = "{{API_KEY}}";

        public WoolBallSpeechToTextWebService()
        {
            _httpClient = new HttpClient();
            _httpClient.DefaultRequestHeaders.Add("Authorization", $"Bearer {API_KEY_PLACEHOLDER}");
        }

        /// <summary>
        /// Transcribes audio from a URL
        /// </summary>
        /// <param name="audioUrl">URL of the audio file to transcribe</param>
        /// <param name="model">Optional: Speech recognition model to use</param>
        /// <param name="language">Optional: Target language for transcription (e.g. "pt" for Portuguese)</param>
        /// <param name="returnTimestamps">Optional: Whether to return timestamps for each segment</param>
        /// <returns>Transcription result containing text and optional timestamps</returns>
        public async Task<TranscriptionResult> TranscribeFromUrlAsync(
            string audioUrl,
            TranscriptionOptions options = null)
        {
            options ??= new TranscriptionOptions();

            var requestUrl = $"{BaseUrl}/speech-to-text?model={Uri.EscapeDataString(options.Model)}&language={options.Language}&returnTimestamps={options.ReturnTimestamps}&webvtt={options.Webvtt}";
            
            var content = new StringContent(
                JsonSerializer.Serialize(new { url = audioUrl }),
                System.Text.Encoding.UTF8,
                "application/json");

            var response = await _httpClient.PostAsync(requestUrl, content);
            response.EnsureSuccessStatusCode();
            
            var jsonResponse = await response.Content.ReadAsStringAsync();
            return JsonSerializer.Deserialize<TranscriptionResult>(jsonResponse);
        }

        /// <summary>
        /// Transcribes audio from a file
        /// </summary>
        /// <param name="audioData">Byte array containing the audio or video file data</param>
        /// <param name="model">Optional: Speech recognition model to use</param>
        /// <param name="language">Optional: Target language for transcription (e.g. "pt" for Portuguese)</param>
        /// <param name="returnTimestamps">Optional: Whether to return timestamps for each segment</param>
        /// <returns>Transcription result containing text and optional timestamps</returns>
        public async Task<TranscriptionResult> TranscribeFromFileAsync(
            byte[] audioData,
            TranscriptionOptions options = null)
        {
            options ??= new TranscriptionOptions();

            var requestUrl = $"{BaseUrl}/speech-to-text?model={Uri.EscapeDataString(options.Model)}&language={options.Language}&returnTimestamps={options.ReturnTimestamps}&webvtt={options.Webvtt}";
            
            var content = new ByteArrayContent(audioData);
            content.Headers.ContentType = new System.Net.Http.Headers.MediaTypeHeaderValue("audio/mpeg");

            var response = await _httpClient.PostAsync(requestUrl, content);
            response.EnsureSuccessStatusCode();
            
            var jsonResponse = await response.Content.ReadAsStringAsync();
            return JsonSerializer.Deserialize<TranscriptionResult>(jsonResponse);
        }

        /// <summary>
        /// Gets available speech-to-text models
        /// </summary>
        /// <returns>Array of available model names</returns>
        public async Task<string[]> GetAvailableModelsAsync()
        {
            var response = await _httpClient.GetAsync($"{BaseUrl}/speech-to-text-models");
            response.EnsureSuccessStatusCode();
            
            var jsonResponse = await response.Content.ReadAsStringAsync();
            return JsonSerializer.Deserialize<string[]>(jsonResponse);
        }
    }

    public class TranscriptionResult
    {
        [JsonPropertyName("text")]
        public string Text { get; set; }

        [JsonPropertyName("chunks")]
        public TranscriptionChunk[] Chunks { get; set; }

        [JsonPropertyName("webvtt")]
        public string WebVtt { get; set; }
    }

    public class TranscriptionChunk
    {
        [JsonPropertyName("text")]
        public string Text { get; set; }

        [JsonPropertyName("start")]
        public double Start { get; set; }

        [JsonPropertyName("end")]
        public double End { get; set; }
    }
}
