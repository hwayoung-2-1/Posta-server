package com.hwayoung.hwayoungserver.portfolio.domain.model;

import com.hwayoung.hwayoungserver.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "question_cluster_items")
public class QuestionClusterItem extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_cluster_id", nullable = false)
    private QuestionCluster questionCluster;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_message_id", nullable = false)
    private ChatMessage chatMessage;

    @Column(name = "similarity_score")
    private Double similarityScore;

    protected QuestionClusterItem() {
    }

    public QuestionClusterItem(QuestionCluster questionCluster, ChatMessage chatMessage, Double similarityScore) {
        this.questionCluster = questionCluster;
        this.chatMessage = chatMessage;
        this.similarityScore = similarityScore;
    }

    public UUID getId() {
        return id;
    }

    public QuestionCluster getQuestionCluster() {
        return questionCluster;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public Double getSimilarityScore() {
        return similarityScore;
    }
}
