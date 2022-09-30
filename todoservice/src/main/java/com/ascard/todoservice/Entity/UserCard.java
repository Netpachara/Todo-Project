package com.ascard.todoservice.Entity;

import com.ascard.todoservice.Entity.embedded.UserCardID;
import lombok.Data;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "User_Card")
public class UserCard {

    @OneToOne()
    @JoinColumn(name="cardID", insertable = false, updatable = false)
    private Card card;

    @EmbeddedId
    private UserCardID userCardID;


}
