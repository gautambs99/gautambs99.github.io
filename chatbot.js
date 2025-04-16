// ==== Chatbot Integration ====

document.getElementById("chat-icon").addEventListener("click", () => {
    document.getElementById("chat-window").style.display = "block";
});

document.getElementById("close-chat").addEventListener("click", () => {
    document.getElementById("chat-window").style.display = "none";
});

document.getElementById("send-btn").addEventListener("click", () => {
    const input = document.getElementById("chat-input");
    const message = input.value.trim();
    if (!message) return;

    appendMessage("You", message);
    input.value = "";

    fetch("chatbot", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: "query=" + encodeURIComponent(message)
    })
        .then(res => res.json())
        .then(data => {
            appendMessage("Bot", data.response);
        })
        .catch(() => {
            appendMessage("Bot", "Sorry, something went wrong.");
        });
});

function appendMessage(sender, text) {
    const chatBody = document.getElementById("chat-body");
    const msg = document.createElement("div");
    msg.className = "chat-message";
    msg.innerHTML = `<strong>${sender}:</strong> ${text}`;
    chatBody.appendChild(msg);
    chatBody.scrollTop = chatBody.scrollHeight;
}
