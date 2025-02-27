'use client';
import React from 'react';

export default function Usage() {
    async function example1() {
        // Example 1: Basic speech-to-text extraction from URL
        const response1 = await fetch('/api/speech-to-text', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                url: 'https://example.com/audio-pt.mp3'
            })
        });
        const result1 = await response1.json();
        console.log(`Basic transcription: ${result1.text}`);
    }

    async function example2() {
        // Example 2: Speech-to-text extraction from URL with timestamps (English audio)
        const response2 = await fetch('/api/speech-to-text', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                url: 'https://example.com/audio-en.wav',
                language: 'en',
                returnTimestamps: true
            })
        });
        const result2 = await response2.json();
        console.log('\nTranscription with timestamps:');
        for (const chunk of result2.chunks) {
            console.log(`${chunk.start.toFixed(2)}s -> ${chunk.end.toFixed(2)}s: ${chunk.text}`);
        }
    }

    async function example3() {
        // Example 3: Speech-to-text extraction from URL with WebVTT subtitles (Spanish audio)
        const response3 = await fetch('/api/speech-to-text', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                url: 'https://example.com/audio-es.mp4',
                language: 'es',
                returnTimestamps: true,
                webvtt: true
            })
        });
        const result3 = await response3.json();
        console.log(`\nWebVTT Subtitles:\n${result3.webvtt}`);
    }

    async function example4() {
        // Example 4: Speech-to-text extraction from local file
        const audioFile = await fetch('/audio.mp3').then(res => res.blob());
        const response4 = await fetch('/api/speech-to-text?language=pt&returnTimestamps=true&webvtt=true', {
            method: 'POST',
            headers: { 'Content-Type': 'audio/mpeg' },
            body: audioFile
        });
        const result4 = await response4.json();
        console.log(`\nFile transcription:\n${result4.text}`);
    }

    async function example5() {
        // Example 5: Advanced usage with custom options
        const response5 = await fetch('/api/speech-to-text', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                url: 'https://example.com/audio.mp3',
                model: 'onnx-community/whisper-large-v3-turbo_timestamped',
                language: 'pt',
                returnTimestamps: true,
                webvtt: true
            })
        });
        const result5 = await response5.json();
        console.log(`\nAdvanced transcription:\n${result5.text}`);
    }

    return (
        <div style={{ padding: '20px' }}>
            <h1>WoolBall Speech-to-Text API Examples</h1>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                <button onClick={example1}>Run Example 1: Basic URL</button>
                <button onClick={example2}>Run Example 2: URL with Timestamps</button>
                <button onClick={example3}>Run Example 3: URL with WebVTT</button>
                <button onClick={example4}>Run Example 4: Local File</button>
                <button onClick={example5}>Run Example 5: Advanced Options</button>
            </div>
            <p>Check the browser console for results</p>
        </div>
    );
}
