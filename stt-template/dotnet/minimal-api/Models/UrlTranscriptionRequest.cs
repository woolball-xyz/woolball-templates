namespace WoolBall.SpeechToText.Models
{
    public class UrlTranscriptionRequest
    {
        public string Url { get; set; }
        public string Model { get; set; }
        public string Language { get; set; }
        public bool ReturnTimestamps { get; set; }
        public bool Webvtt { get; set; }
    }
}
