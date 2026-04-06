import { useState } from "react";
import ReactMarkdown from "react-markdown";

function App() {
  const [message, setMessage] = useState("");
  const [messages, setMessages] = useState([]);
  const [controller, setController] = useState(null);
  const [isStreaming, setIsStreaming] = useState(false);
  const [mode, setMode] = useState("chat");

  const sendMessage = async () => {
    if (!message || isStreaming) return;

    const userMessage = { role: "user", content: message };

    setMessages((prev) => [...prev, userMessage]);
    setMessage("");

    const abortController = new AbortController();
    setController(abortController);
    setIsStreaming(true);

    const res = await fetch(
      `http://localhost:8080/api/chat/stream?mode=${mode}`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify([...messages, userMessage]),
        signal: abortController.signal,
      },
    );

    const reader = res.body.getReader();
    const decoder = new TextDecoder();

    let aiMessage = { role: "ai", content: "" };
    setMessages((prev) => [...prev, aiMessage]);

    try {
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        const chunk = decoder.decode(value);
        aiMessage.content += chunk;

        setMessages((prev) => {
          const updated = [...prev];
          updated[updated.length - 1] = { ...aiMessage };
          return updated;
        });
      }
    } catch (err) {
      if (err.name !== "AbortError") {
        console.error("Streaming error:", err);
      }
    }

    setIsStreaming(false);
    setController(null);
  };

  const stopStreaming = () => {
    if (controller) {
      controller.abort();
      setIsStreaming(false);
    }
  };

  const uploadFile = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file);

    await fetch("http://localhost:8080/api/docs/upload", {
      method: "POST",
      body: formData,
    });

    alert("File uploaded successfully!");
  };

  return (
    <div style={{ padding: "20px", fontFamily: "Arial" }}>
      <h2>AI Assistant</h2>

      {/* Mode Toggle */}
      <div style={{ marginBottom: "10px" }}>
        <button onClick={() => setMode("chat")} disabled={mode === "chat"}>
          Chat
        </button>

        <button
          onClick={() => setMode("docs")}
          disabled={mode === "docs"}
          style={{ marginLeft: "10px" }}
        >
          Docs
        </button>
      </div>

      {/* File Upload (for RAG) */}
      {mode === "docs" && (
        <div style={{ marginBottom: "10px" }}>
          <input type="file" onChange={uploadFile} />
        </div>
      )}

      {/* Input */}
      <input
        value={message}
        onChange={(e) => setMessage(e.target.value)}
        onKeyDown={(e) => {
          if (e.key === "Enter") {
            sendMessage();
          }
        }}
        placeholder="Ask something..."
        style={{ width: "300px", padding: "8px" }}
      />

      <button
        onClick={sendMessage}
        disabled={isStreaming}
        style={{ marginLeft: "10px" }}
      >
        Send
      </button>

      <button
        onClick={stopStreaming}
        disabled={!isStreaming}
        style={{ marginLeft: "10px" }}
      >
        Stop
      </button>

      {/* Chat Messages */}
      <div style={{ marginTop: "20px" }}>
        {messages.map((msg, index) => (
          <div key={index} style={{ marginBottom: "10px" }}>
            <strong>{msg.role === "user" ? "You" : "AI"}:</strong>
            <div>
              <ReactMarkdown>{msg.content}</ReactMarkdown>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default App;
