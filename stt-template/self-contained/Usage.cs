using System;
using System.IO;
using System.Threading.Tasks;
using WoolBall.SpeechToText;

class Program
{
    static async Task Main()
    {
        // Initialize the speech-to-text service
        var service = new WoolBallSpeechToTextWebService();

        try
        {
            // Example 1: Basic speech-to-text extraction from URL
            var result1 = await service.TranscribeFromUrlAsync(
                "https://example.com/audio-pt.mp3"
            );
            Console.WriteLine($"Basic transcription: {result1.Text}");

            // Example 2: Speech-to-text extraction from URL with timestamps (English audio)
            var result2 = await service.TranscribeFromUrlWithTimestampsAsync(
                "https://example.com/audio-en.wav",
                language: "en"
            );
            Console.WriteLine("\nTranscription with timestamps:");
            foreach (var chunk in result2.Chunks)
            {
                Console.WriteLine($"{chunk.Start:0.00}s -> {chunk.End:0.00}s: {chunk.Text}");
            }

            // Example 3: Speech-to-text extraction from URL with WebVTT subtitles (Spanish audio)
            var result3 = await service.TranscribeFromUrlWithWebVttAsync(
                "https://example.com/audio-es.mp4",
                language: "es"
            );
            Console.WriteLine($"\nWebVTT Subtitles:\n{result3.WebVtt}");

            // Example 4: Speech-to-text extraction from local file
            byte[] audioData = File.ReadAllBytes("audio.mp3");
            var result4 = await service.TranscribeFromFileWithWebVttAsync(
                audioData,
                language: "pt"
            );
            
            // Save the subtitles to a .vtt file
            File.WriteAllText("subtitles.vtt", result4.WebVtt);
            Console.WriteLine("\nSubtitles saved to 'subtitles.vtt'");

            // Example 5: Advanced usage with custom options
            var result5 = await service.TranscribeFromUrlAsync(
                "https://example.com/audio.mp3",
                new TranscriptionOptions
                {
                    Model = "onnx-community/whisper-large-v3-turbo_timestamped",
                    Language = "pt",
                    ReturnTimestamps = true,
                    Webvtt = true
                }
            );
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Erro: {ex.Message}");
        }
    }
}
