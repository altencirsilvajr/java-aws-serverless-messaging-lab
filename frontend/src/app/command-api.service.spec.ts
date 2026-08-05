import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { CommandApiService } from './command-api.service';

describe('CommandApiService', () => {
  let service: CommandApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(CommandApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('submits the selected failure mode to the real API contract', () => {
    service.submit('msg-1', 'work', true).subscribe();
    const request = http.expectOne('/api/commands');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ messageId: 'msg-1', payload: 'work', simulateFailure: true });
    request.flush({ command: {}, duplicate: false });
  });
});
