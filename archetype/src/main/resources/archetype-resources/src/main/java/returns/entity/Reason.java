#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.returns.entity;

/** Powod, dla ktorego customerId zwraca lines. */
public enum Reason {
    DAMAGED,
    WRONG_ITEM,
    NOT_AS_DESCRIBED,
    CHANGED_MIND,
    OTHER
}
