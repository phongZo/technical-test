package org.example.dto;

import java.util.List;

public class HuobiResponseDto {
    private String status;
    private List<HuobiTickerDto> data;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<HuobiTickerDto> getData() { return data; }
    public void setData(List<HuobiTickerDto> data) { this.data = data; }
}
