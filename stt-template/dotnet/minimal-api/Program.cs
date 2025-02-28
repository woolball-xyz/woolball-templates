using WoolBall.SpeechToText;
using WoolBall.SpeechToText.Models;

var builder = WebApplication.CreateBuilder(args);

// Add services
builder.Services.AddSingleton<WoolBall.SpeechToText.WoolBallSpeechToTextWebService>();
builder.Services.AddEndpointsApiExplorer();

// Configure CORS
builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

var app = builder.Build();

// Configure middleware
app.UseCors();

// Configure endpoints
app.MapGet("/", () => "WoolBall Speech-to-Text API");

// Speech-to-text endpoint for URL-based transcription
app.MapPost("/speech-to-text/url", async (WoolBall.SpeechToText.WoolBallSpeechToTextWebService service, 
    HttpRequest request) =>
{
    try
    {
        var requestData = await request.ReadFromJsonAsync<UrlTranscriptionRequest>();
        
        if (string.IsNullOrEmpty(requestData?.Url))
        {
            return Results.BadRequest(new { error = "No URL provided" });
        }

        var options = new WoolBall.SpeechToText.TranscriptionOptions
        {
            Model = requestData.Model ?? "onnx-community/whisper-large-v3-turbo_timestamped",
            Language = requestData.Language ?? "pt",
            ReturnTimestamps = requestData.ReturnTimestamps,
            Webvtt = requestData.Webvtt
        };

        var result = await service.TranscribeFromUrlAsync(requestData.Url, options);
        return Results.Ok(result);
    }
    catch (Exception ex)
    {
        return Results.Problem(ex.Message, statusCode: 500);
    }
});

// Speech-to-text endpoint for file-based transcription
app.MapPost("/speech-to-text/file", async (WoolBall.SpeechToText.WoolBallSpeechToTextWebService service, 
    HttpRequest request) =>
{
    try
    {
        if (!request.HasFormContentType)
        {
            return Results.BadRequest(new { error = "Request must be multipart/form-data" });
        }

        var form = await request.ReadFormAsync();
        var file = form.Files.GetFile("audio");
        
        if (file == null)
        {
            return Results.BadRequest(new { error = "No audio file provided" });
        }

        // Read file into memory
        using var memoryStream = new MemoryStream();
        await file.CopyToAsync(memoryStream);
        var audioData = memoryStream.ToArray();

        // Parse options from form
        var options = new WoolBall.SpeechToText.TranscriptionOptions
        {
            Model = form["model"].FirstOrDefault() ?? "onnx-community/whisper-large-v3-turbo_timestamped",
            Language = form["language"].FirstOrDefault() ?? "pt",
            ReturnTimestamps = bool.TryParse(form["returnTimestamps"].FirstOrDefault(), out bool returnTimestamps) && returnTimestamps,
            Webvtt = bool.TryParse(form["webvtt"].FirstOrDefault(), out bool webvtt) && webvtt
        };

        var result = await service.TranscribeFromFileAsync(audioData, options);
        return Results.Ok(result);
    }
    catch (Exception ex)
    {
        return Results.Problem(ex.Message, statusCode: 500);
    }
});

// Get available speech-to-text models
app.MapGet("/speech-to-text-models", async (WoolBall.SpeechToText.WoolBallSpeechToTextWebService service) =>
{
    try
    {
        var models = await service.GetAvailableModelsAsync();
        return Results.Ok(models);
    }
    catch (Exception ex)
    {
        return Results.Problem(ex.Message, statusCode: 500);
    }
});

app.Run();
