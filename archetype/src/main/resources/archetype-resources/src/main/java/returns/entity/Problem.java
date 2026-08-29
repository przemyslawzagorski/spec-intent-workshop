#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.returns.entity;

/** Ksztalt bledu wedlug kontraktu (docs/contract/openapi.yaml). */
public record Problem(String title, int status, String detail) {
}
