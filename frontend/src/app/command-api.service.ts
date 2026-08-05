import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export type ProcessingStatus = 'ACCEPTED' | 'PROCESSING' | 'COMPLETED' | 'FAILED';

export interface ProcessingRecord {
  messageId: string;
  payload: string;
  status: ProcessingStatus;
  attempts: number;
  duplicateCount: number;
  simulateFailure: boolean;
  lastError: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CommandResponse {
  command: ProcessingRecord;
  duplicate: boolean;
}

export interface QueueStatus {
  pending: number;
  deadLetter: number;
}

@Injectable({ providedIn: 'root' })
export class CommandApiService {
  private readonly http = inject(HttpClient);

  submit(messageId: string, payload: string, simulateFailure: boolean): Observable<CommandResponse> {
    return this.http.post<CommandResponse>('/api/commands', { messageId, payload, simulateFailure });
  }

  find(messageId: string): Observable<CommandResponse> {
    return this.http.get<CommandResponse>(`/api/commands/${encodeURIComponent(messageId)}`);
  }

  duplicate(messageId: string): Observable<unknown> {
    return this.http.post(`/api/commands/${encodeURIComponent(messageId)}/duplicate`, {});
  }

  queues(): Observable<QueueStatus> {
    return this.http.get<QueueStatus>('/api/commands/operations/queues');
  }
}
