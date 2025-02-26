using System;
using System.IO;
using System.Threading.Tasks;
using WoolBall.SpeechToText;

class Program
{
    static async Task Main()
    {
        // Inicializar o serviço
        var service = new WoolBallSpeechToTextWebService();

        try
        {
            // Exemplo 1: Transcrição básica de URL
            var result1 = await service.TranscribeFromUrlAsync(
                "https://example.com/audio-pt.mp3"
            );
            Console.WriteLine($"Transcrição básica: {result1.Text}");

            // Exemplo 2: Transcrição de URL com timestamps em inglês
            var result2 = await service.TranscribeFromUrlWithTimestampsAsync(
                "https://example.com/audio-en.mp3",
                language: "en"
            );
            Console.WriteLine("\nTranscrição com timestamps:");
            foreach (var chunk in result2.Chunks)
            {
                Console.WriteLine($"{chunk.Start:0.00}s -> {chunk.End:0.00}s: {chunk.Text}");
            }

            // Exemplo 3: Transcrição de URL com legendas WebVTT em espanhol
            var result3 = await service.TranscribeFromUrlWithWebVttAsync(
                "https://example.com/audio-es.mp3",
                language: "es"
            );
            Console.WriteLine($"\nLegendas WebVTT:\n{result3.WebVtt}");

            // Exemplo 4: Transcrição de arquivo local
            byte[] audioData = File.ReadAllBytes("audio.mp3");
            var result4 = await service.TranscribeFromFileWithWebVttAsync(
                audioData,
                language: "pt"
            );
            
            // Salvar as legendas em um arquivo .vtt
            File.WriteAllText("legendas.vtt", result4.WebVtt);
            Console.WriteLine("\nLegendas salvas em 'legendas.vtt'");

            // Exemplo 5: Uso avançado com opções personalizadas
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
