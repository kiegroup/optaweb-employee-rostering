export function modulo(n: number, base: number) {
  const positiveBase = Math.abs(base);
  return ((n % positiveBase) + positiveBase) % positiveBase;
}
