package org.example.bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.State;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TgUser {
    private Long id;
    private String name;
    private State state=State.START;
    private String phone;
    private Integer lastMessageId;
    private List<Integer> lastMessageIds=new ArrayList<>();
}
