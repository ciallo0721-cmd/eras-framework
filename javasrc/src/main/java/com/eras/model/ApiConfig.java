package com.eras.model;

/**
 * AI配置模型 - 对应 options/api.json
 */
public class ApiConfig {
    private String provider = "openai";
    private String api_key = "";
    private String api_url = "https://api.openai.com/v1";
    private String model = "gpt-4";
    private double temperature = 0.8;
    private int max_tokens = 2048;
    private String description = "支持 openai 和 ollama 两种 provider";

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getApiKey() { return api_key; }
    public void setApiKey(String apiKey) { this.api_key = apiKey; }
    public String getApiUrl() { return api_url; }
    public void setApiUrl(String apiUrl) { this.api_url = apiUrl; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public int getMaxTokens() { return max_tokens; }
    public void setMaxTokens(int maxTokens) { this.max_tokens = maxTokens; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
