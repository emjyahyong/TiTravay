package com.titravay.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    /** Null = non lu par le destinataire. Renseigné quand l'autre participant ouvre la conversation. */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    public Message() {}

    public Message(Conversation conversation, User sender, String content, LocalDateTime timestamp) {
        this.conversation = conversation;
        this.sender       = sender;
        this.content      = content;
        this.timestamp    = timestamp;
    }

    // ── Getters / Setters ────────────────────────────────────

    public Long getId()                        { return id; }

    public Conversation getConversation()      { return conversation; }
    public void setConversation(Conversation c){ this.conversation = c; }

    public User getSender()                    { return sender; }
    public void setSender(User sender)         { this.sender = sender; }

    public String getContent()                 { return content; }
    public void setContent(String content)     { this.content = content; }

    public LocalDateTime getTimestamp()              { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp){ this.timestamp = timestamp; }

    public LocalDateTime getReadAt()                 { return readAt; }
    public void setReadAt(LocalDateTime readAt)      { this.readAt = readAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message m)) return false;
        return id != null && id.equals(m.id);
    }

    @Override
    public int hashCode() {
        return 31 + (id != null ? id.hashCode() : 0);
    }
}
