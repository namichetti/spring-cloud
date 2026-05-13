package com.amichetti.api.event;

import static java.time.ZonedDateTime.now;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import java.time.ZonedDateTime;

public class Event<K, T> {

  public enum Type {
    CREATE,
    DELETE
  }

  //Tipo de evento, por ejemplo create o delete evento.
  private final Type eventType;
  //Clave que identifica la data, por ejemplo, bookId
  private final K key;
  //Es la data del evento.
  private final T data;
  //Cuándo ocurrió el evento
  private final ZonedDateTime eventCreatedAt;

  public Event() {
    this.eventType = null;
    this.key = null;
    this.data = null;
    this.eventCreatedAt = null;
  }

  public Event(Type eventType, K key, T data) {
    this.eventType = eventType;
    this.key = key;
    this.data = data;
    this.eventCreatedAt = now();
  }

  public Type getEventType() {
    return eventType;
  }

  public K getKey() {
    return key;
  }

  public T getData() {
    return data;
  }

  @JsonSerialize(using = ZonedDateTimeSerializer.class)
  public ZonedDateTime getEventCreatedAt() {
    return eventCreatedAt;
  }
}
