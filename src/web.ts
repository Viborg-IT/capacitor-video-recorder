import { WebPlugin } from '@capacitor/core';

import type { VideoRecorderPlugin } from './definitions';

export class VideoRecorderWeb extends WebPlugin implements VideoRecorderPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
