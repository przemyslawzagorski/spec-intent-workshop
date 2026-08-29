#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.returns.entity;

/** Kto placi za przesylke zwrotna. */
public enum Payer {
    MERCHANT,
    CUSTOMER
}
