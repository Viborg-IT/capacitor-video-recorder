export interface VideoRecorderPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
